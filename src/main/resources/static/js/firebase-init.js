const firebaseConfig = {
  apiKey: "AIzaSyBDKIU4hFmo8yvQdMo4TKhE71Lnhz-6J94",
  authDomain: "json-store-52d9e.firebaseapp.com",
  projectId: "json-store-52d9e",
  storageBucket: "json-store-52d9e.appspot.com",
  messagingSenderId: "953170806887",
  appId: "1:953170806887:web:897b3f9e35b0a1f0e22cc2",
  measurementId: "G-PF428EC1GP"
};

firebase.initializeApp(firebaseConfig);
firebase.auth().signInAnonymously().then(() => {
  console.log("익명 로그인 성공");
}).catch((error) => {
  console.error("익명 로그인 실패:", error);
});

window.messaging = firebase.messaging();
