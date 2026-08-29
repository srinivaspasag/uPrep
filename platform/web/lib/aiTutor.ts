import { ObjectId, type Db } from "mongodb";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveBoardNames } from "@/lib/legacyBoard";

// Shared AI Tutor ("Aira") core — used both by the doubt-creation flow
// (auto-answers every new doubt) and any future manual re-ask affordance.
// Groq hosts open-weight models (Llama, Mixtral, ...) on custom hardware —
// fast enough for a synchronous request, and free-tier, unlike a paid
// frontier-model API. OpenAI-compatible chat-completions endpoint, so a
// plain fetch is enough — no SDK dependency needed.
const GROQ_API_KEY = process.env.GROQ_API_KEY || "";
const GROQ_MODEL = process.env.GROQ_MODEL || "openai/gpt-oss-120b";
const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

export type AnswerStep = { title: string; body: string };
export type GuidedAnswer = { steps: AnswerStep[]; confidence: "high" | "low"; reasoning: string };
export type AnswerMode = "detailed" | "short" | "guided";

const MODE_INSTRUCTIONS: Record<AnswerMode, string> = {
  guided:
    "Give a GUIDED, step-by-step walkthrough — the student clicks through your steps one at a time, so break " +
    "your reasoning into 2-6 genuinely separate steps (e.g. identify what's being asked, set up the approach, " +
    "work through it, state the result — adapt the actual steps to what the question needs) rather than one " +
    "long paragraph pretending to be a single step. Each step should teach one idea and build on the last.",
  detailed:
    "Give ONE thorough, in-depth explanation covering the full reasoning end to end — return exactly one step " +
    "(title can just describe the topic) whose body is the complete, detailed answer, well-organized with clear " +
    "paragraphs. This is read as a single block, not clicked through, so don't withhold parts of the reasoning " +
    "for a later step.",
  short:
    "Give ONE short, concise key-summary answer — return exactly one step (title can just describe the topic) " +
    "whose body is a brief answer: the essential result and the minimum reasoning needed to justify it, a few " +
    "sentences at most. No lengthy derivation.",
};

function toId(id: string): ObjectId | string {
  return ObjectId.isValid(id) ? new ObjectId(id) : id;
}

function stripHtml(s: unknown): string {
  if (typeof s !== "string") return "";
  return s.replace(/<[^>]*>/g, " ").replace(/&nbsp;/g, " ").replace(/\s+/g, " ").trim();
}

export function aiTutorConfigured(): boolean {
  return !!GROQ_API_KEY;
}

// Grounds the model in the doubt's own tagged chapter (its boardIds) plus a
// few real questions from that chapter, rather than letting it answer from
// open-ended recall, and forces a self-reported confidence via tool-use so
// a shaky answer can be gated behind a teacher review instead of shown
// outright. See the design note this implements: the AI is always labeled
// and never a substitute for a human answering — it just answers first.
async function generateGuidedAnswer(
  question: string,
  chapterName: string | null,
  sampleQuestions: string[],
  mode: AnswerMode
): Promise<GuidedAnswer> {
  const context = chapterName
    ? `This doubt was asked under the chapter "${chapterName}". Stay within that chapter's syllabus scope.`
    : "This doubt has no chapter tag — answer only if the topic is unambiguous, otherwise say you're unsure what topic this is.";
  const examples = sampleQuestions.length
    ? `For calibration, here are a few real questions already in this chapter's question bank (do not answer these, they're just to show you the chapter's level and style):\n${sampleQuestions
        .map((q, i) => `${i + 1}. ${q}`)
        .join("\n")}`
    : "";

  const res = await fetch(GROQ_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${GROQ_API_KEY}` },
    body: JSON.stringify({
      model: GROQ_MODEL,
      max_tokens: 1500,
      messages: [
        {
          role: "system",
          content:
            `You are Aira, a tutor answering a single student doubt on a study platform. ${MODE_INSTRUCTIONS[mode]} ` +
            "Never invent a formula, fact, or numeric result you are not sure of — if you are not confident in " +
            "the answer, say so plainly and set confidence to \"low\" rather than presenting a guess as fact. " +
            "$ $ / $$ $$ wrap ONLY the mathematical expression itself — never English words or phrases. Close " +
            "the $ immediately after the symbols end, before continuing in plain text; never leave a $ open " +
            "across a parenthetical remark or explanation. Correct: \"the sum is $\\frac{b}{a}$ (since the " +
            "coefficient of x is $-b$) and the product is $\\frac{c}{a}$.\" Wrong: \"the sum is " +
            "$\\frac{b}{a}(\\text{since the coefficient of x is} -b)\\text{and the product is}\\frac{c}{a}$.\" " +
            "Never use \\( \\) or \\[ \\] delimiters, the renderer only recognizes $ and $$.",
        },
        { role: "user", content: `${context}\n\n${examples}\n\nStudent's doubt:\n${question}` },
      ],
      tools: [
        {
          type: "function",
          function: {
            name: "provide_answer",
            description:
              "Provide a grounded, guided step-by-step answer to the student's doubt with a self-assessed " +
              "confidence level.",
            parameters: {
              type: "object",
              properties: {
                steps: {
                  type: "array",
                  minItems: 1,
                  maxItems: 6,
                  description: "One or more steps, per the instructions above for the requested answer style.",
                  items: {
                    type: "object",
                    properties: {
                      title: {
                        type: "string",
                        description:
                          "Short label for this step (3-6 words), e.g. \"Set up the equation\" — do NOT prefix " +
                          "with \"Step 1\" etc, the UI already numbers steps.",
                      },
                      body: { type: "string", description: "This step's explanation. Can include LaTeX math." },
                    },
                    required: ["title", "body"],
                  },
                },
                confidence: {
                  type: "string",
                  enum: ["high", "low"],
                  description:
                    "\"high\" only if you are certain the answer is fully correct and directly grounded in the " +
                    "given chapter context. \"low\" if there is any real doubt, ambiguity in the question, or it " +
                    "falls outside the given chapter scope — low-confidence answers are held for a teacher to " +
                    "review before students see them, so default to \"low\" whenever unsure.",
                },
                reasoning: { type: "string", description: "One or two sentences on why this confidence level was chosen." },
              },
              required: ["steps", "confidence", "reasoning"],
            },
          },
        },
      ],
      tool_choice: { type: "function", function: { name: "provide_answer" } },
    }),
  });

  if (!res.ok) throw new Error(`Groq API error (${res.status}): ${(await res.text()).slice(0, 300)}`);
  const data = await res.json();
  const call = data.choices?.[0]?.message?.tool_calls?.[0];
  if (!call) throw new Error("Model did not return a structured answer");
  const parsed = JSON.parse(call.function.arguments) as GuidedAnswer;
  if (!Array.isArray(parsed.steps) || parsed.steps.length === 0) throw new Error("Model returned no steps");
  // The model is told to use $ $/$$ $$ (the only delimiters the app's KaTeX
  // renderer recognizes) but open-weight models often default to \( \)/\[ \]
  // regardless of instruction — normalize here rather than trust compliance.
  const denormalize = (s: string) =>
    s.replace(/\\\[/g, "$$").replace(/\\\]/g, "$$").replace(/\\\(/g, "$").replace(/\\\)/g, "$");
  // The model is also told the UI already numbers each step, but it often
  // prefixes the title with "Step N:" anyway, which would double up against
  // GuidedAnswer's own "Step N of M —" label — strip that redundant prefix.
  const stripStepPrefix = (s: string) => s.replace(/^\s*step\s*\d+\s*[:.\-–—]?\s*/i, "");
  parsed.steps = parsed.steps.map((s) => ({
    title: stripStepPrefix(denormalize(s.title)),
    body: denormalize(s.body),
  }));
  // Enforced here rather than trusted from the prompt: "detailed"/"short"
  // must never render as a click-through stepper (the client decides
  // stepper-vs-single-block purely from steps.length), so collapse to one
  // step regardless of how many the model actually returned.
  if (mode !== "guided" && parsed.steps.length > 1) {
    parsed.steps = [
      {
        title: parsed.steps[0].title,
        body: parsed.steps.map((s) => s.body).join("\n\n"),
      },
    ];
  }
  return parsed;
}

// Generates and stores Aira's answer for a given doubt, idempotently (a
// second call for the same doubt reuses whatever's already there instead of
// spawning a duplicate). Never throws — a Groq hiccup never blocks the
// doubt itself from being posted — but DOES log the real failure (was
// previously swallowed with no trace at all, which made a genuine API
// failure indistinguishable from a deliberate low-confidence hold: both
// left the doubt with no visible AI answer and the exact same "wasn't
// confident enough" message client-side). Returns whether an answer now
// exists, so callers can tell "worked" from "silently didn't" and offer a
// retry instead of a misleading confidence explanation.
export async function ensureAiAnswer(db: Db, doubtId: string, mode: AnswerMode = "guided"): Promise<boolean> {
  if (!GROQ_API_KEY) return false;
  try {
    const existing = await db
      .collection("comments")
      .findOne({ entityId: doubtId, entityType: "DISCUSSION", userId: "ai-tutor", recordState: "ACTIVE" });
    if (existing) return true;

    const doubt: any = await db.collection("discussions").findOne({ _id: toId(doubtId) as any });
    if (!doubt) return false;

    const orgId = doubt.contentSrc?.id || DEFAULT_ORG_ID;
    const boardIds: string[] = Array.isArray(doubt.boardIds) ? doubt.boardIds.filter(Boolean) : [];

    let chapterName: string | null = null;
    if (boardIds.length) {
      const names = await resolveBoardNames(orgId, boardIds);
      chapterName = names[boardIds[boardIds.length - 1]] || null;
    }

    let sampleQuestions: string[] = [];
    if (boardIds.length) {
      const docs = await db
        .collection("questions")
        .find({ boardIds: { $in: boardIds }, "contentSrc.id": orgId, recordState: "ACTIVE" })
        .limit(3)
        .toArray();
      sampleQuestions = (docs as any[]).map((q) => stripHtml(q.content)).filter(Boolean);
    }

    const question = [doubt.name, doubt.content].filter(Boolean).join("\n\n");
    // Groq occasionally emits a tool call whose arguments aren't valid JSON
    // (confirmed live: "Failed to parse tool call arguments as JSON", a
    // 400 from Groq's own side, not a bug in this code) — non-deterministic
    // sampling noise, not a real content problem, so a fresh attempt almost
    // always succeeds. Retry a couple of times before actually giving up.
    let result: GuidedAnswer | null = null;
    let lastError: unknown = null;
    for (let attempt = 1; attempt <= 3 && !result; attempt++) {
      try {
        result = await generateGuidedAnswer(question, chapterName, sampleQuestions, mode);
      } catch (e) {
        lastError = e;
        console.error(`[aiTutor] generateGuidedAnswer attempt ${attempt} failed`, {
          doubtId,
          mode,
          error: e instanceof Error ? e.message : e,
        });
      }
    }
    if (!result) throw lastError instanceof Error ? lastError : new Error("Aira failed after 3 attempts");
    const flattened =
      mode === "guided" && result.steps.length > 1
        ? result.steps.map((s, i) => `Step ${i + 1}: ${s.title}\n${s.body}`).join("\n\n")
        : result.steps.map((s) => s.body).join("\n\n");

    const now = Date.now();
    const _id = new ObjectId();
    const pending = result.confidence !== "high";
    const doc: Record<string, unknown> = {
      _id,
      entityId: doubtId,
      entityType: "DISCUSSION",
      content: flattened,
      userId: "ai-tutor",
      userName: "Aira",
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
      aiMeta: {
        model: GROQ_MODEL,
        mode,
        confidence: result.confidence,
        reasoning: result.reasoning,
        groundedOn: chapterName,
        steps: result.steps,
      },
    };
    if (pending) doc.status = "pending_review";
    await db.collection("comments").insertOne(doc as any);

    // Only bump the visible answer count once the answer is actually
    // visible — a pending one isn't counted until a teacher approves it.
    if (!pending) {
      await db
        .collection("discussions")
        .updateOne({ _id: toId(doubtId) as any }, { $inc: { comments: 1 }, $set: { lastUpdated: now } });
    }
    return true;
  } catch (e) {
    // Doesn't throw — the doubt still exists and can be retried later —
    // but DOES log, so a real Groq failure shows up in `docker logs
    // uprep-ui-1` instead of vanishing with zero trace.
    console.error("[aiTutor] ensureAiAnswer failed", { doubtId, mode, error: e instanceof Error ? e.message : e });
    return false;
  }
}
