# Epik Hub — Firebase Production / Development Seed Data Guide

This document provides the exact collection names, document IDs, field names, data types, and instructions required to populate Epik Hub with the initial pilot data for **Epik Compu-Tech** and its four initial services in Cloud Firestore.

---

## 1. Collection: `companies`

### Document ID: `epik_compu_tech`

| Field Name | Data Type | Value / Example | Description |
|---|---|---|---|
| `id` | String | `"epik_compu_tech"` | Primary Key / Document ID match |
| `name` | String | `"Epik Compu-Tech"` | Public Company Display Name |
| `description` | String | `"Premier provider of computer repair & maintenance, gadget wrapping & customization, graphic design, and web/software development."` | Business Overview |
| `phone` | String | `"+265 (880) 551-373"` | Customer Support Contact Phone |
| `email` | String | `"epikcomputech@gmail.com"` | Official Business Email |
| `location` | String | `"Zingwangwa, Blantyre"` | Physical Address / Operating Location |
| `logoUrl` | String (or Null) | `"https://res.cloudinary.com/cjyra9sc/image/upload/v1795260437/1005753143_1.png"` | Logo Asset URL |
| `status` | String | `"active"` | Company Platform Status (`"active"` or `"inactive"`) |
| `createdAt` | Number (Long timestamp) | `1700000000000` | Creation Epoch Milliseconds |
| `updatedAt` | Number (Long timestamp) | `1700000000000` | Last Update Epoch Milliseconds |

---

## 2. Collection: `services`

### Service 1: Computer Repair & Maintenance
- **Document ID**: `epik_service_1`

| Field Name | Data Type | Value (MWK)                                                                                                               |
|---|---|---------------------------------------------------------------------------------------------------------------------------|
| `id` | String | `"epik_service_1"`                                                                                                        |
| `companyId` | String | `"epik_compu_tech"`                                                                                                       |
| `name` | String | `"Computer Repair & Maintenance"`                                                                                         |
| `description` | String | `"Full hardware diagnostics, virus removal, OS re-installation, laptop screen replacement, and thermal paste repasting."` |
| `category` | String | `"Hardware & Maintenance"`                                                                                                |
| `price` | Number (Double) | `30000.00`                                                                                                                |
| `imageUrl` | String | `"https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=500"`                                                    |
| `isActive` | Boolean | `true`                                                                                                                    |
| `companyActive` | Boolean | `true`                                                                                                                    |
| `createdAt` | Number (Long timestamp) | `1700000000000`                                                                                                           |
| `updatedAt` | Number (Long timestamp) | `1700000000000`                                                                                                           |

### Service 2: Gadget Wrapping & Customization
- **Document ID**: `epik_service_2`

| Field Name | Data Type | Value (MWK)                                                                                          |
|---|---|------------------------------------------------------------------------------------------------------|
| `id` | String | `"epik_service_2"`                                                                                   |
| `companyId` | String | `"epik_compu_tech"`                                                                                  |
| `name` | String | `"Gadget Wrapping & Customization"`                                                                  |
| `description` | String | `"Precision vinyl wrapping for laptops, smartphones, gaming consoles, and custom skin application."` |
| `category` | String | `"Customization"`                                                                                    |
| `price` | Number (Double) | `25000.00`                                                                                           |
| `imageUrl` | String | `"https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500"`                               |
| `isActive` | Boolean | `true`                                                                                               |
| `companyActive` | Boolean | `true`                                                                                               |
| `createdAt` | Number (Long timestamp) | `1700000000000`                                                                                      |
| `updatedAt` | Number (Long timestamp) | `1700000000000`                                                                                      |

### Service 3: Graphic Design
- **Document ID**: `epik_service_3`

| Field Name | Data Type | Value (MWK)                                                                                            |
|---|---|--------------------------------------------------------------------------------------------------------|
| `id` | String | `"epik_service_3"`                                                                                     |
| `companyId` | String | `"epik_compu_tech"`                                                                                    |
| `name` | String | `"Graphic Design"`                                                                                     |
| `description` | String | `"Professional logo design, brand identity creation, social media banners, and marketing collateral."` |
| `category` | String | `"Design"`                                                                                             |
| `price` | Number (Double) | `50000.00`                                                                                             |
| `imageUrl` | String | `"https://images.unsplash.com/photo-1626785774573-4b799315345d?w=500"`                                 |
| `isActive` | Boolean | `true`                                                                                                 |
| `companyActive` | Boolean | `true`                                                                                                 |
| `createdAt` | Number (Long timestamp) | `1700000000000`                                                                                        |
| `updatedAt` | Number (Long timestamp) | `1700000000000`                                                                                        |

### Service 4: Web & Software Development
- **Document ID**: `epik_service_4`

| Field Name | Data Type | Value (MWK)                                                                                               |
|---|---|-----------------------------------------------------------------------------------------------------------|
| `id` | String | `"epik_service_4"`                                                                                        |
| `companyId` | String | `"epik_compu_tech"`                                                                                       |
| `name` | String | `"Web & Software Development"`                                                                            |
| `description` | String | `"Custom Android mobile apps, responsive web applications, API integrations, and database architecture."` |
| `category` | String | `"Software"`                                                                                              |
| `price` | Number (Double) | `250000.00`                                                                                               |
| `imageUrl` | String | `"https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500"`                                       |
| `isActive` | Boolean | `true`                                                                                                    |
| `companyActive` | Boolean | `true`                                                                                                    |
| `createdAt` | Number (Long timestamp) | `1700000000000`                                                                                           |
| `updatedAt` | Number (Long timestamp) | `1700000000000`                                                                                           |

---

## 3. Provisioning Company Administrator Accounts

To assign an admin user to Epik Compu-Tech:
1. Create a Firebase Authentication user account in the Firebase Console (e.g. Email: `epikcomputech@gmail.com`).
2. Copy the generated Auth User UID (e.g., `YOUR_ADMIN_AUTH_UID_HERE`).
3. In Cloud Firestore collection `users`, create a document whose **Document ID** is the exact Auth User UID:

### Collection: `users`
- **Document ID**: `YOUR_ADMIN_AUTH_UID_HERE` (Matches Auth UID)

| Field Name | Data Type | Value |
|---|---|---|
| `uid` | String | `"YOUR_ADMIN_AUTH_UID_HERE"` |
| `fullName` | String | `"Epik Compu-Tech Admin"` |
| `email` | String | `"epikcomputech@gmail.com"` |
| `phone` | String | `"+265 (880) 551-373"` |
| `role` | String | `"company_admin"` |
| `companyId` | String | `"epik_compu_tech"` |
| `createdAt` | Number (Long timestamp) | `1700000000000` |
| `updatedAt` | Number (Long timestamp) | `1700000000000` |

When this administrator logs into the Epik Hub mobile application, the app fetches `users/YOUR_ADMIN_AUTH_UID_HERE`, detects `role == 'company_admin'` and `companyId == 'epik_compu_tech'`, and routes them directly to the **Company Management Dashboard** to manage Epik Compu-Tech's services and requests.
