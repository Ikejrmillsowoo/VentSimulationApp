
# 🫁 Ventilator Simulation App

A full-stack web application that simulates how patients respond to different ventilator settings in real-time. Built for educational and training purposes, this is perfect for clinicians, respiratory therapists, students, and simulation developers.

---

## 🔧 Features

- 🖥️ **Interactive Web UI** (React + Bootstrap)
  - Adjust ventilator settings (mode, tidal volume, rate, PEEP, FiO₂, pressures)
  - Choose patient scenarios: Normal, ARDS, COPD, and others
  - See real-time ABG results and feedback
  - Responsive design for desktop/tablet/mobile
  - https://github.com/Ikejrmillsowoo/ventSim-front

- ⚙️ **Simulation Engine** (Spring Boot)
  - Backend logic simulates gas exchange and patient response
  - Supports multiple ventilation modes: Volume Control, Pressure Control, CPAP, Pressure Support
  - Scenario-based logic: changes behavior based on selected patient profile

- 🧠 **Physiological Modeling**
  - Uses simplified formulas for compliance, resistance, CO₂ elimination, and oxygenation
  - Predicts changes to ABG values based on user input

---

## 🚀 Tech Stack

| Frontend      | Backend       | Other Tools        |
|---------------|---------------|--------------------|
| React         | Spring Boot   | Netlify / Render   |
| Bootstrap 5   | Java 17       | GitHub             |
| Fetch         | RESTful API   | JSON for defaults  |

---

## 🩺 Patient Scenarios Included

- **Normal Lungs**
- **ARDS** – poor compliance and high shunt
- **COPD** – high resistance, chronic CO₂ retention
- **Edge Cases** – hypoventilation, overventilation, low oxygenation, high pressures

---

## 🧪 How It Works

1. Choose a patient condition (e.g., ARDS)
2. Default ventilator settings are loaded
3. Modify any setting (e.g., increase FiO₂ or decrease tidal volume)
4. Click "Simulate"
5. View:
   - ABG values: pH, PaCO₂, PaO₂, HCO₃⁻, SaO₂
   - Feedback: warnings or suggestions based on physiology
   - Mode-specific logic (Volume vs Pressure modes, etc.)

---

## 🛠️ Installation & Run

### 🔹 Backend (Spring Boot)

```bash
cd ventilator-simulation-backend
./mvnw spring-boot:run
# or use your IDE to run VentilatorSimulationApp.java
```

### 🔹 Frontend (React)

```bash
cd ventilator-simulation-frontend
npm install
npm start
```

> ⚠️ Ensure your backend is running at `http://localhost:8080` or update the API URL in your frontend accordingly.

---

## 🌐 API Endpoint

`POST /api/simulate`

**Request Body Example:**

```json
{
  "scenario": "ARDS",
  "tidalVolume": 400,
  "respiratoryRate": 16,
  "peep": 12,
  "fio2": 60,
  "mode": "Volume Control"
}
```

**Response:**

```json
{
  "abg": {
    "pH": 7.38,
    "paCO2": 45,
    "paO2": 75,
    "hco3": 24,
    "saO2": 94,
    "be": 0
  },
  "feedback": "Moderate hypoxia. Consider increasing PEEP or FiO₂..",
  "status": "warning"
}
```

---

## 📄 License

This project is open-source and intended for educational purposes. I would appreciate it if you used it in your own tools, apps, or curriculum.

---

## 🙌 Contributing / Future Plans

Planned features:
- ABG trend graphing
- Patient vitals (HR, BP)
- Dynamic compliance/resistance updates
- Authentication for saving scenarios

Contributions and forks welcome!
