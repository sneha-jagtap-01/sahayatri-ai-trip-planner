# 🌿 Sahayatri \| AI Trip Planner

Sahayatri is an AI-powered trip planning web application that creates
personalized travel itineraries based on **mood, budget, number of days,
location, and travel preferences**.

## ✨ Key Features

- 🎭 Mood-based planning: Chill, Adventure, Spiritual, Romantic
- 🗺️ AI-generated day-wise itineraries
- 📍 Real places and local food recommendations
- 💰 Budget estimation for accommodation, food, transport, and
  activities
- 🛡️ AI-assisted safety/readiness score
- 🤖 AI trip explanations and travel tips
- 📸 Instagram Reel screenshot scanner for location detection
- 👥 Group Budget Splitter with friends and expenses
- 💸 UPI payment intent for settlements
- 💬 TripBot for budget-splitting questions
- ❤️ Save trips to My Trips
- 🔄 Regenerate Trip
- 📄 Export Trip as PDF
- 🔐 Login, Signup, and Profile
- 📱 Responsive frontend

## 🧠 AI Integration

Sahayatri uses **Google Gemini** for personalized trip explanations,
travel tips, mood-based recommendations, and Reel screenshot location
analysis.

Real place and restaurant data is also used to make the generated trip
more practical.

## 🛠️ Tech Stack

**Frontend** - HTML5 - CSS3 - JavaScript - LocalStorage - Google Maps
Embed

**Backend** - Java - Spring Boot - Gradle - REST APIs

**Database** - MySQL - Spring Data JPA / Hibernate

**APIs & Services** - Google Places API - Google Maps Embed API - Google
Gemini API

## 🔄 How It Works

``` text
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
  ├── Map
  ├── Budget
  ├── Safety / Readiness Score
  ├── Local Food
  └── AI Tips
```

## 📸 Reel Scanner

``` text
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

## 💰 Budget Splitter

The Budget Splitter helps friends manage trip expenses.

It supports: - Creating a trip group - Adding friends - Recording
expenses and who paid - Viewing expense history - Calculating total and
equal shares - Showing settlements - Opening UPI payment intent where
supported - Asking TripBot about the split

## 📁 Project Structure

``` text
Sahayatri/
├── backend/
│   └── sahayatri-backend/
│       ├── src/
│       ├── build.gradle
│       ├── settings.gradle
│       └── gradlew
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
└── .gitignore
```

## ⚙️ How to Run Locally

### 1. Clone the repository

``` bash
git clone https://github.com/sneha-jagtap-01/sahayatri-ai-trip-planner.git
cd sahayatri-ai-trip-planner
```

### 2. Configure the backend

Create:

``` text
backend/sahayatri-backend/src/main/resources/application.properties
```

Use `application-example.properties` as the template and add your own
local database/API credentials.

**Never commit `application.properties` or API keys to GitHub.**

### 3. Start MySQL

Create or use a MySQL database named:

``` text
sahayatri
```

### 4. Run the backend

Windows:

``` powershell
cd backend/sahayatri-backend
.\gradlew.bat bootRun
```

The backend runs on:

``` text
http://localhost:8080
```

### 5. Run the frontend

Open the `frontend` folder in VS Code and run `index.html` using a local
server such as Live Server.

## 🔐 Security Note

The real `application.properties` file is excluded from this repository.

For public deployment: - Restrict and rotate API keys when necessary. -
Use environment variables or a secure secret manager. - Use HTTPS. -
Replace localStorage-based authentication with secure session/JWT
authentication. - Add stronger backend authorization and ownership
checks.

## 🚀 Future Scope

- 📱 Android / iOS app
- 💬 Official WhatsApp Business API integration
- 🧭 Advanced route optimization
- 🌦️ Live weather
- 🚆 Live transport and traffic information
- 🏨 Hotel and stay recommendations
- 🎟️ Booking integrations
- ☁️ Cloud deployment
- 🔒 Production-grade authentication
- 📊 Travel analytics and trip insights

## 🎯 Project Goal

Sahayatri aims to make trip planning **personalized, practical, and
simple** by combining:

**Mood + AI + Real Places + Budget + Maps + Group Expense Management**

instead of providing a generic travel itinerary.

## 👩‍💻 Developer

**Sneha Jagtap**  
Third-Year Computer Engineering Student

GitHub: https://github.com/sneha-jagtap-01

------------------------------------------------------------------------

⭐ If you find this project interesting, consider giving the repository
a star!
