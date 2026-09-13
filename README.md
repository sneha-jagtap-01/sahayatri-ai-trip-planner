# 🌿 Sahayatri | AI Trip Planner

**Sahayatri** is an AI-powered travel planning web application that creates personalized trip plans based on the user's **mood, location, budget, number of days, and travel preferences**.

Instead of generating a generic itinerary, Sahayatri combines **Mood + AI + Real Places + Budget + Maps + Group Expense Management** to make trip planning more personalized and practical.

## 🌐 Live Demo

🚀 **Live Website:**  
https://sahayatri-ai-trip-planner-1.onrender.com

> The backend is deployed separately using Spring Boot and Render.

---

## ✨ Key Features

- 🎭 **Mood-Based Trip Planning**
  - Chill
  - Adventure
  - Spiritual
  - Romantic

- 🗺️ **AI-Generated Day-Wise Itinerary**
- 📍 **Real Destination Recommendations**
- 🍴 **Local Food & Restaurant Recommendations**
- 💰 **Budget Estimation**
  - Accommodation
  - Food
  - Transport
  - Activities

- 🛡️ **AI-Assisted Safety / Readiness Score**
- 🤖 **AI Trip Explanation & Travel Tips**
- 📸 **Instagram Reel Screenshot Scanner**
- 👥 **Group Budget Splitter**
- 💸 **Expense Settlement & UPI Payment Intent**
- 💬 **TripBot for Budget-Splitting Questions**
- ❤️ **Save Trips to My Trips**
- 🔄 **Regenerate Trip**
- 📄 **Export Trip as PDF**
- 🔐 **Login & Signup**
- 👤 **User Profile**
- 📷 **Profile Photo**
- 📱 **Responsive Web Interface**

---

## 🧠 AI Integration

Sahayatri uses **Google Gemini** to provide intelligent travel assistance.

AI is used for:

- Personalized trip explanations
- Travel tips
- Mood-based recommendations
- Preference-based suggestions
- Instagram Reel screenshot location analysis

The application also uses real place and restaurant data to make generated itineraries more practical.

---

## 🛠️ Tech Stack

### Frontend

- HTML5
- CSS3
- JavaScript
- LocalStorage
- Google Maps Embed

### Backend

- Java
- Spring Boot
- Gradle
- REST APIs

### Database

- MySQL
- Spring Data JPA
- Hibernate

### APIs & Services

- Google Places API
- Google Maps Embed API
- Google Gemini API

### Deployment

- Render
- Aiven MySQL

---

## 🔄 How It Works

```text
User
  ↓
Mood + Location + Budget + Days + Preferences
  ↓
Spring Boot Backend
  ↓
Real Places + Restaurants + AI Processing
  ↓
Personalized Itinerary
  ↓
Dashboard
  ├── Day-wise Plan
  ├── Google Map
  ├── Budget
  ├── Safety / Readiness Score
  ├── Local Food
  └── AI Travel Tips
```

---

## 📸 Instagram Reel Scanner

The Reel Scanner allows users to analyze a travel-related Instagram Reel screenshot and detect the location using AI.

```text
Instagram Reel Screenshot
          ↓
      Reel Scanner
          ↓
    AI Location Detection
          ↓
    Detected Location
          ↓
       Plan My Trip
          ↓
   Personalized Trip
```

---

## 💰 Group Budget Splitter

The Budget Splitter helps friends manage expenses during a group trip.

### Features

- Create a trip group
- Add friends
- Record trip expenses
- Track who paid
- View expense history
- Calculate total expenses
- Calculate equal shares
- Show settlements
- Open UPI payment intent where supported
- Ask TripBot questions about the expense split

---

## 📁 Project Structure

```text
Sahayatri/
│
├── backend/
│   └── sahayatri-backend/
│       ├── src/
│       ├── build.gradle
│       ├── settings.gradle
│       ├── gradlew
│       └── Dockerfile
│
├── frontend/
│   ├── index.html
│   ├── dashboard.html
│   ├── explore.html
│   ├── budget-splitter.html
│   ├── my-trips.html
│   ├── profile.html
│   ├── login.html
│   ├── signup.html
│   ├── about.html
│   ├── script.js
│   ├── style.css
│   └── dashboard.css
│
├── .gitignore
└── README.md
```

---

## ⚙️ How to Run Locally

### 1. Clone the Repository

```bash
git clone https://github.com/sneha-jagtap-01/sahayatri-ai-trip-planner.git
cd sahayatri-ai-trip-planner
```

### 2. Configure the Backend

Create:

```text
backend/sahayatri-backend/src/main/resources/application.properties
```

Use your local database and API credentials.

For security reasons, **never commit `application.properties`, passwords, or API keys to GitHub.**

### 3. Start MySQL

Create or use a MySQL database for the application.

Example:

```text
Database: sahayatri
```

### 4. Run the Backend

On Windows:

```powershell
cd backend/sahayatri-backend
.\gradlew.bat bootRun
```

The local backend runs on:

```text
http://localhost:8080
```

### 5. Run the Frontend

Open the `frontend` folder in VS Code and run `index.html` using a local development server such as **Live Server**.

---

## 🔐 Security

The real `application.properties` file is excluded from the repository.

For production environments:

- Keep API keys private
- Use environment variables or a secret manager
- Restrict API key permissions
- Rotate exposed credentials when necessary
- Use HTTPS
- Implement secure session/JWT-based authentication
- Add stronger backend authorization and ownership validation

---

## 🚀 Future Scope

Sahayatri can be further enhanced with:

- 📱 Android / iOS mobile application
- 💬 Official WhatsApp Business API integration
- 🧭 Advanced route optimization
- 🌦️ Live weather information
- 🚆 Live transport and traffic information
- 🏨 Hotel and stay recommendations
- 🎟️ Booking integrations
- 🔒 Production-grade authentication
- 📊 Travel analytics and trip insights
- 🗺️ Advanced interactive maps

---

## 🎯 Project Goal

The goal of Sahayatri is to make travel planning **personalized, practical, and simple**.

The application combines:

> **Mood + AI + Real Places + Budget + Maps + Group Expense Management**

to provide a smarter alternative to generic travel itineraries.

---

## 👩‍💻 Developer

### Sneha Jagtap

**Third-Year Computer Engineering Student**

GitHub:  
https://github.com/sneha-jagtap-01

---

## ⭐ Support

If you find **Sahayatri** interesting, consider giving the repository a ⭐ star!
