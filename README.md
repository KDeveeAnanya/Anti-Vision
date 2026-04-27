# Anti-Vision

> A behavior-driven journaling system designed to prevent negative life patterns before they happen.

## What is Anti-Vision?

Traditional productivity apps focus on what you *should* do, relying on motivation and discipline—two resources that often deplete. Anti-Vision takes a different approach, grounded in behavioral psychology:

- **Loss Aversion:** We are naturally more motivated to avoid pain than to acquire gains. Anti-Vision leverages this by focusing on identifying and mitigating self-destructive behaviors.
- **Implementation Intentions (If/Then Rules):** Instead of vague goals, the system forces you to define strict rules: *"If I experience [Trigger], then I will [Preventive Action] to avoid [Negative Consequence]."* This bypasses decision fatigue by pre-loading your response to temptation.

## Tech Stack

- **Frontend:** React + Tailwind CSS (High-fidelity, atmospheric UI)
- **Backend:** Java 17 + Spring Boot 3 (Robust, strictly-typed API)
- **Database:** PostgreSQL (Relational data persistence)
- **AI Layer:** NVIDIA NIM API / Llama 3.1 8B Instruct (Behavioral pattern extraction)

## System Architecture

The application follows a clean, decoupled client-server architecture:

```text
User Input 
  → React (Frontend Validation) 
  → Spring Boot Controller (REST API) 
  → Service Layer (Business Logic) 
  → NVIDIA Llama 3.1 AI (Pattern Extraction via HTTP) 
  → PostgreSQL (Persistence via Hibernate/JPA) 
  → Response (JSON Rule DTO to React)
```

## Features

1. **AI-Powered Pattern Extraction:** Automatically distills chaotic journal entries into clear triggers, emotions, and consequences using Llama 3.1.
2. **If/Then Rule Generation:** Converts your vulnerabilities into actionable implementation intentions, giving you a tangible playbook against bad habits.
3. **Atmospheric, Distraction-Free UI:** A brutalist, dark-mode focused interface that forces introspection without the gamified clutter of modern apps.

## How to Run Locally

### Prerequisites
- Node.js & npm
- Java 17 & Maven
- PostgreSQL running locally (`localhost:5432`)
- NVIDIA API Key

### Backend Setup (Spring Boot)

1. Open your terminal and navigate to the backend directory:
   ```bash
   cd spring-backend
   ```
2. Set your NVIDIA API key as an environment variable (PowerShell):
   ```powershell
   $env:NVIDIA_API_KEY="your_nvidia_api_key_here"
   ```
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(The backend will start on `http://localhost:8080`)*

### Frontend Setup (React)

1. Open a new terminal tab and navigate to the frontend directory:
   ```bash
   cd client
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   *(The frontend will start on `http://localhost:5173`)*

## API Documentation

### Analyze Reflection

Analyzes a raw journal entry to extract behavioral insights.

**Endpoint:** `POST /api/reflections`

**Request Body:**
```json
{
  "content": "I felt incredibly stressed after the meeting today, so I ended up mindlessly scrolling on my phone for 3 hours instead of working. Now my whole evening is ruined."
}
```

**Response:**
```json
{
  "trigger": "phone",
  "emotion": "stressed",
  "consequence": "ruined",
  "preventiveRule": "IF I am tempted by [phone], THEN I will immediately step away for 5 minutes to avoid [ruined].",
  "earlyWarning": "Red Flag: When I feel [stressed], my brain will likely seek out [phone] as a coping mechanism."
}
```

## Why This Project?

Anti-Vision is an exercise in **constraint-based design** and **psychology-driven architecture**. It deliberately rejects the "hustle culture" philosophy of endless to-do lists and gamification. By forcing the user to confront their negative patterns, the software acts as a mirror rather than a cheerleader. It was built not just to demonstrate full-stack proficiency, but to solve a genuine problem: the gap between knowing what not to do, and actually not doing it.
