# Chat Application

> 🚧 **Under Active Development** - This project is currently being built and features are being added regularly.

A real-time chat application built with Spring Boot, featuring JWT authentication, WebSocket messaging, and friend management system.

## 🚀 Features

- ✅ JWT Authentication (Login/Register)
- ✅ Real-time messaging via WebSocket (STOMP)
- ✅ Friend system (send/accept/reject friend requests)
- ✅ Friends-only chat restriction
- 🚧 More features coming soon...

## 🛠 Tech Stack

- **Backend:** Spring Boot 3.x
- **Security:** Spring Security + JWT
- **WebSocket:** STOMP protocol
- **Database:** MySQL/PostgreSQL with JPA/Hibernate
- **Documentation:** Swagger/OpenAPI

## 📋 Prerequisites

- Java 17+
- Maven or Gradle
- MySQL 8.0+ or PostgreSQL 12+

## ⚡ Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/chat-app.git
cd chat-app
```

2. **Configure database** in `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chatapp
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your-secret-key-minimum-256-bits
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Friends
- `POST /api/friends/request/{userId}` - Send friend request
- `POST /api/friends/request/{id}/accept` - Accept request
- `POST /api/friends/request/{id}/reject` - Reject request
- `GET /api/friends` - Get friends list
- `GET /api/friends/requests/pending` - Get pending requests
- `DELETE /api/friends/{friendId}` - Remove friend

### Users
- `GET /api/users` - Get all users

### WebSocket
- Connect: `/ws`
- Send message: `/app/private`
- Subscribe: `/user/queue/private`

> **Note:** All friend endpoints require JWT token in `Authorization: Bearer <token>` header

## 🗄 Database Schema

The application uses 4 main tables:
- `users` - User accounts
- `friend_requests` - Friend request workflow (PENDING/ACCEPTED/REJECTED)
- `friendships` - Accepted friendships (one record per friendship)
- `messages` - Chat messages

## 🔐 Authentication

1. Register or login to get JWT token
2. Include token in Authorization header: `Bearer <your-token>`
3. Token is valid for 24 hours (configurable)

## 🧪 Testing

Test the API using Swagger UI or any REST client:

1. Login via `/api/auth/login`
2. Copy the JWT token
3. Click "Authorize" in Swagger UI and paste the token
4. Try the friend endpoints

## 📝 Project Status

- [x] Authentication system
- [x] Friend request workflow
- [x] Real-time messaging
- [x] Friend management
- [ ] Group chat
- [ ] File sharing
- [ ] Notifications
- [ ] User profiles

## 🤝 Contributing

This project is under development. Contributions, issues, and feature requests are welcome!

## 📄 License

MIT License

---

**Built with Spring Boot** ⚡
