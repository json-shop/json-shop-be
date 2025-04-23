importScripts('https://www.gstatic.com/firebasejs/9.18.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.18.0/firebase-messaging-compat.js');

const firebaseConfig = {
    apiKey: "AIzaSyCpRQAOsd_YPJYn3nSOqUyJ4qCEbndgNec",
    authDomain: "json-store-94fa8.firebaseapp.com",
    projectId: "json-store-94fa8",
    storageBucket: "json-store-94fa8.firebasestorage.app",
    messagingSenderId: "799710081526",
    appId: "1:799710081526:web:db1486e12ac4f49623d7d7",
    measurementId: "G-4MW54E47HT"
};

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
