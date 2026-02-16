# DAFPP Supermarket App 

DAFPP Supermarket is a comprehensive Android application designed to streamline the shopping experience for customers and the management process for administrators. The app features branch-specific shopping, M-Pesa payment integration, and real-time order management.

## Features

### User Roles
* **Customer:**
    * Browse products by specific branch (Nairobi HQ, Kisumu, Mombasa, Nakuru, Eldoret).
    * Add items to a cart and view total costs.
    * **M-Pesa Integration:** Seamless payment via STK Push (Daraja API).
    * Order Tracking.
* **Admin:**
    * **Restock Inventory:** Add new products or update existing stock levels.
    * **Manage Orders:** View incoming orders, see customer details, and update order status (Pending → Shipped → Delivered).
    * **View Reports:** Analyze sales data and inventory status.

### Security & UX
* **Firebase Authentication:** Secure Login and Registration.
* **Inactivity Timeout:** Automatic logout/redirection if the app is left inactive for 1 hour to protect user data.
* **Input Security:** Hidden password fields and validated inputs.

## Tech Stack

* **Language:** Java
* **Frontend:** XML (Android Layouts)
* **Backend:** Firebase Firestore (NoSQL Database)
* **Auth:** Firebase Authentication
* **Payments:** Safaricom Daraja API (M-Pesa STK Push)
* **IDE:** Android Studio

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine.

### Prerequisites
* Android Studio (Latest Version)
* Java Development Kit (JDK)
* A Firebase Project
* Safaricom Daraja Developer Account

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/yourusername/dafpp-supermarket.git](https://github.com/yourusername/dafpp-supermarket.git)
    ```
2.  **Open in Android Studio:**
    Open Android Studio -> File -> Open -> Select the project folder.

3.  **Firebase Setup:**
    * Go to your [Firebase Console](https://console.firebase.google.com/).
    * Download the `google-services.json` file.
    * Paste it into the `app/` folder of your project.

4.  **M-Pesa Configuration:**
    * Open `DarajaApiClient.java`.
    * Replace the `CONSUMER_KEY` and `CONSUMER_SECRET` with your credentials from the [Safaricom Developer Portal](https://developer.safaricom.co.ke/).

5.  **Build and Run:**
    * Sync Gradle files.
    * Run the app on an Emulator or Physical Device.

##  Project Structure

* `MainActivity.java`: The central hub that routes users based on their role (Admin vs Customer).
* `DarajaApiClient.java`: Handles all M-Pesa API logic and STK Pushes.
* `AdminOrdersActivity.java`: The dashboard for admins to process orders.
* `DafppApp.java`: Background service that monitors user inactivity.

##  Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

##  License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

##  Author

**Francis Muhengere**
* Developer & Maintainer
