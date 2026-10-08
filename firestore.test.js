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
const ADMIN_UID = "admin_999";

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

test("Unauthenticated user cannot read wallpapers", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("wallpapers").get());
});

test("Authenticated user can read published wallpapers", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("wallpapers").doc("wp1").set({
      id: "wp1",
      title: "Neon Cyberpunk",
      category: "Anime",
      cloudinaryUrl: "https://res.cloudinary.com/demo/image/upload/sample.jpg",
      cloudinaryPublicId: "root_dr/sample",
      thumbnailUrl: "https://res.cloudinary.com/demo/image/upload/c_thumb,w_400/sample.jpg",
      uploadedAt: new Date(),
      isPublished: true,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("wallpapers").doc("wp1").get());
});

test("Non-admin user cannot create wallpaper", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("wallpapers").doc("wp2").set({
      id: "wp2",
      title: "Hacked Wallpaper",
      category: "Anime",
      cloudinaryUrl: "https://res.cloudinary.com/demo/image/upload/sample.jpg",
      cloudinaryPublicId: "root_dr/sample",
      thumbnailUrl: "https://res.cloudinary.com/demo/image/upload/sample.jpg",
      uploadedAt: new Date(),
      isPublished: true,
    })
  );
});

test("Admin user (lisaamane001@gmail.com) can create wallpaper", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "lisaamane001@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("wallpapers").doc("wp3").set({
      id: "wp3",
      title: "Admin Nature Sunset",
      category: "Nature",
      cloudinaryUrl: "https://res.cloudinary.com/demo/image/upload/sunset.jpg",
      cloudinaryPublicId: "root_dr/sunset",
      thumbnailUrl: "https://res.cloudinary.com/demo/image/upload/c_thumb/sunset.jpg",
      uploadedAt: new Date(),
      isPublished: true,
    })
  );
});

test("User can read and write only their own favorites", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("favorites").doc("fav1").set({
      id: "fav1",
      userId: ALICE_UID,
      wallpaperId: "wp1",
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("favorites").doc("fav1").get());
});
