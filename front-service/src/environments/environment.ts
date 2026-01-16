// src/environments/environment.ts

// export const environment = {
//   production: false,
//   apiUrl: 'http://localhost:8080/api', // url de api gateway

//   // URLs des microservices (via API Gateway)
//   services: {
//     user: '/users',
//     listing: '/listings',
//     booking: '/bookings',
//     payment: '/payments',
//     messaging: '',
//     notification: '/notifications',
//     review: '/reviews',
//     media: '/media',
//     blockchain: '/blockchain',

//   },

//   // Configuration Blockchain
//   blockchain: {
//     chainId: '0xaa36a7', // 11155111 = Sepolia
//     chainName: 'Sepolia Test Network',
//     rpcUrl: 'https://sepolia.infura.io/v3/50e3d616c7ae4bb4bb5d48d97ab6d5a8'
//   },

//   // Configuration Socket.io
//   socketUrl: 'http://localhost:8085', // Messaging Service

//   //wsUrl: 'ws://localhost:8080/messaging-service/ws', // WebSocket pour temps réel

//   // Configuration Upload
//   maxFileSize: 5 * 1024 * 1024, // 5 MB
//   allowedImageTypes: ['image/jpeg', 'image/png', 'image/webp'],

//   // Autres configs
//   tokenKey: 'authToken', // le token jwt
//   userKey: 'current_user',
//   wsUrl: 'ws://localhost:8080/api/ws/messages',
// };


export const environment = {
  production: false,
  apiUrl: '/api',

  services: {
    user: '/users',
    listing: '/listings',
    booking: '/bookings',
    payment: '/payments',
    messaging: '',
    notification: '/notifications',
    review: '/reviews',
    media: '/media',
    blockchain: '/blockchain',
  },

   blockchain: {
    chainId: '0xaa36a7', // 11155111 = Sepolia
    chainName: 'Sepolia Test Network',
    rpcUrl: 'https://sepolia.infura.io/v3/50e3d616c7ae4bb4bb5d48d97ab6d5a8'
  },

  socketUrl: '/api/messages',
  maxFileSize: 5 * 1024 * 1024,
  allowedImageTypes: ['image/jpeg', 'image/png', 'image/webp'],
  tokenKey: 'authToken',
  userKey: 'current_user',
  wsUrl: '/api/ws/messages'
};
