# 🚀 SmartTaskManager v1.0

**SmartTaskManager** is a modern desktop application built with **JavaFX** and **MySQL**, designed to revolutionize productivity through an integrated **AI Chatbot**. It combines traditional task management with intelligent insights to help users "Eat the Frog" and stay on track.

## 👥 Developed By

This project was developed as part of our final year project by:

* **[Atiqa Essayouti]** -* **[Sana Timourti]** *[Kaoutar Misbah] 


---

## ✨ Key Features

* **🧠 AI-Powered Insights**: Real-time productivity analysis and task suggestions using a custom `AIService`.
* **📅 Dynamic Calendar**: Visual task tracking and scheduling.
* **💬 Intelligent Chatbot**: An interactive assistant that provides motivational advice and detects task priorities using NLP.
* **💾 Robust Persistence**: Fully integrated with **MySQL** for secure data storage.
* **📦 Professional Distribution**: Packaged as a standalone **Windows Executable (.exe)**.

## 🛠️ Technical Stack

* **Language**: Java 21
* **Framework**: JavaFX (UI/UX)
* **Database**: MySQL (XAMPP)
* **Build Tool**: Maven
* **Deployment**: jpackage & WiX Toolset

## 🚀 Installation & Deployment

The application is distributed as a standalone installer to ensure the best user experience.

1.  Navigate to the **[Releases](https://github.com/atiqaessayouti/SmartTaskManager1/releases)** section.
2.  Download the `SmartTaskManager-1.0.exe`.
3.  Run the installer and follow the instructions.
*(Note: Ensure MySQL is running via XAMPP for database connectivity).*

## 🏗️ Architecture

The project follows a clean **MVC (Model-View-Controller)** pattern and uses **Multi-threading** to ensure the AI logic does not block the UI thread.
