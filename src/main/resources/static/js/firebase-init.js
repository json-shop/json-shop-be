const firebaseConfig = {
  apiKey: "AIzaSyCpRQAOsd_YPJYn3nSOqUyJ4qCEbndgNec",
  authDomain: "json-store-94fa8.firebaseapp.com",
  projectId: "json-store-94fa8",
  storageBucket: "json-store-94fa8.firebasestorage.app",
  messagingSenderId: "799710081526",
  appId: "1:799710081526:web:db1486e12ac4f49623d7d7",
  measurementId: "G-4MW54E47HT"
};

firebase.initializeApp(firebaseConfig);
firebase.auth().signInAnonymously().then(() => {
  console.log("익명 로그인 성공");
}).catch((error) => {
  console.error("익명 로그인 실패:", error);
});

window.messaging = firebase.messaging();
