# Skill Share Platform - Backend

This is the backend service for the Skill Share Platform, built with Spring Boot. It provides RESTful APIs for user management, skill sharing, learning plans, and social interactions.

## Features

- User authentication with JWT and OAuth2 (Google, Facebook)
- Post creation and management with media upload support
- Learning plan creation and tracking
- Social features (follow, like, comment)
- Real-time notifications
- File upload handling

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher

## Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd skill-share-platform/backend
```

2. Create a PostgreSQL database:
```sql
CREATE DATABASE skillshare;
```

3. Configure application properties:
   - Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`
   - Update the database connection settings
   - Set your JWT secret key
   - Configure OAuth2 credentials for Google and Facebook

4. Build the project:
```bash
mvn clean install
```

5. Run the application:
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080/api`

## API Documentation

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login with email and password
- `GET /api/auth/me` - Get current user info

### Posts
- `GET /api/posts` - Get all posts
- `GET /api/posts/feed` - Get posts from followed users
- `POST /api/posts` - Create a new post
- `PUT /api/posts/{id}` - Update a post
- `DELETE /api/posts/{id}` - Delete a post
- `POST /api/posts/{id}/like` - Like a post
- `DELETE /api/posts/{id}/like` - Unlike a post

### Learning Plans
- `GET /api/learning-plans` - Get all learning plans
- `GET /api/learning-plans/my-plans` - Get user's learning plans
- `POST /api/learning-plans` - Create a learning plan
- `PUT /api/learning-plans/{id}` - Update a learning plan
- `DELETE /api/learning-plans/{id}` - Delete a learning plan

### Users
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update user profile
- `POST /api/users/me/profile-picture` - Update profile picture
- `POST /api/users/{id}/follow` - Follow a user
- `DELETE /api/users/{id}/follow` - Unfollow a user
- `GET /api/users/search` - Search users

### Notifications
- `GET /api/notifications` - Get user notifications
- `GET /api/notifications/unread-count` - Get unread notifications count
- `POST /api/notifications/mark-all-read` - Mark all notifications as read
- `DELETE /api/notifications/clear-read` - Clear read notifications

## Security

The application uses JWT for authentication and implements OAuth2 for social login. All endpoints except for authentication and public post viewing require authentication.

## File Upload

The application supports file uploads for:
- Post media (images and short videos)
- User profile pictures

Files are stored in the configured upload directory and are served through the application.

## Error Handling

The application implements global error handling and returns appropriate HTTP status codes and error messages.

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request 