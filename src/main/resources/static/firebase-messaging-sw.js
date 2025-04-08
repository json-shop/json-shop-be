importScripts('https://www.gstatic.com/firebasejs/9.18.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.18.0/firebase-messaging-compat.js');

firebase.initializeApp({
    apiKey: "AIzaSyBDKIU4hFmo8yvQdMo4TKhE71Lnhz-6J94",
    authDomain: "json-store-52d9e.firebaseapp.com",
    projectId: "json-store-52d9e",
    storageBucket: "json-store-52d9e.appspot.com",
    messagingSenderId: "953170806887",
    appId: "1:953170806887:web:897b3f9e35b0a1f0e22cc2"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신:', payload);
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body
        // icon: '/notification-icon.png'
    };
    self.registration.showNotification(notificationTitle, notificationOptions);
});

self.addEventListener('notificationclick', (event) => {
    event.notification.close();
    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
            if (clientList.length > 0) {
                return clientList[0].focus();
            }
            return clients.openWindow('/');
        })
    );
});
