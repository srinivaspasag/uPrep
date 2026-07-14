// Deterministic demo seed: publishes the CMDS-authored test into the live
// content-services collections (tests, questions) that the student app reads.
// This mirrors exactly what the publish pipeline would write, bypassing the
// two known legacy blockers (Play 2.1 SrcEntity binding bug + mongo-driver
// 2.10.1 collectionExists() incompatibility with MongoDB 3.4).

var ORG = "5874a52bc92ed65e3defc7e5";
var USER = "6a3a96810cf26ac35629e399";
var now = new Date().getTime();

var BRD_KIN = "6a3b7ab30cf2f6add23ae039"; // Kinematics
var BRD_PHY = "6a3b7ab30cf2f6add23ae035"; // Physics (course)
var BRD_LOM = "6a3b7ab30cf2f6add23ae038"; // Laws of Motion

function q(id, text, options, answerIdx, boardIds) {
    return {
        _id: ObjectId(id),
        content: text,
        type: "SCQ",
        options: options,
        answerId: String(answerIdx),
        hasAns: true,
        solutions: NumberLong(0),
        attempts: NumberLong(0),
        code: "",
        difficulty: "EASY",
        boardIds: boardIds,
        contentSrc: { type: "ORGANIZATION", id: ORG },
        scope: "ORG",
        userId: USER,
        timeCreated: NumberLong(now),
        lastUpdated: NumberLong(now),
        recordState: "ACTIVE"
    };
}

var questions = [
    q("6a3b7c970cf2d6b30f9e2fe5",
      "A ball is thrown horizontally from a height. Ignoring air resistance, its horizontal acceleration is:",
      ["Zero", "9.8 m/s^2 downward", "9.8 m/s^2 forward", "Variable"], 0,
      [BRD_KIN, BRD_PHY]),
    q("6a3b7c980cf2d6b30f9e2fe8",
      "The area under a velocity-time graph represents:",
      ["Acceleration", "Displacement", "Force", "Power"], 1,
      [BRD_KIN, BRD_PHY]),
    q("6a3b7c990cf2d6b30f9e2feb",
      "Newton's first law of motion is also known as the law of:",
      ["Inertia", "Acceleration", "Action-Reaction", "Gravitation"], 0,
      [BRD_PHY, BRD_LOM])
];

questions.forEach(function (doc) {
    db.questions.save(doc);
    print("seeded question " + doc._id.str);
});

// Publish the test: copy the CMDS-authored test into the live `tests`
// collection with the same _id, marked ACTIVE + published.
var t = db.cmdstests.findOne({ _id: ObjectId("6a3b85780cf2d6b30f9e2fee") });
if (!t) {
    print("ERROR: source cmdstest not found");
} else {
    t.published = true;
    t.recordState = "ACTIVE";
    t.timeCreated = NumberLong(now);
    t.lastUpdated = NumberLong(now);
    delete t.publishingInProgress;
    db.tests.save(t);
    print("published test " + t._id.str + " name=" + t.name);
}

print("---- verify ----");
print("live questions: " + db.questions.count());
print("live tests: " + db.tests.count());
