const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob",
      email: "bob@test.com",
      targetExamination: "UPSC",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can create and read own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      email: "alice@test.com",
      targetExamination: "SSC CGL",
      dailyQuestionTarget: 20,
      preferredDifficulty: "Medium",
      studyStreak: 1,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can add quiz attempt to own subcollection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("quizAttempts").doc("attempt_1").set({
      userId: ALICE_UID,
      quizType: "AI_QUIZ",
      topic: "Indian Economy",
      targetExamination: "SSC CGL",
      totalQuestions: 10,
      attemptedQuestions: 10,
      correctAnswers: 8,
      incorrectAnswers: 2,
      unattemptedQuestions: 0,
      accuracy: 80.0,
      timeTakenSeconds: 300,
      score: 8.0,
      completedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot access Bob's quiz attempts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("quizAttempts").doc("attempt_1").get()
  );
});

test("Authenticated user: can add and query bookmarks in own subcollection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("bm_1").set({
      userId: ALICE_UID,
      contentType: "NEWS",
      contentId: "news_123",
      title: "RBI Monetary Policy Meeting",
      summary: "Repo rate remains unchanged at 6.5%",
      category: "Economy",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").get()
  );
});
