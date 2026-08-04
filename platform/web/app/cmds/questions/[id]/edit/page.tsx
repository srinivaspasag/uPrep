"use client";

import { useParams } from "next/navigation";
import QuestionForm from "../../QuestionForm";

export default function EditQuestionPage() {
  const params = useParams();
  return <QuestionForm questionId={String(params.id)} />;
}
