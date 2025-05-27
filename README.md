# LHV Homework

This repository contains the solution to the [LHV homework assignment](lhv-aml.pdf).

## Overview

The project is composed of the following **Dockerized applications**:

-   **Spring Boot** (Backend API)
-   **React** (Frontend client)
-   **Redis** (In-memory data store)

The application provides functionality to **verify a person's name** against a list of sanctioned individuals using multiple string similarity algorithms (e.g., Jaro-Winkler, Levenshtein, Jaccard, phonetic matching). In addition, it supports basic **CRUD operations** for managing sanctioned names.

The API is accessible via the frontend client, or manually through tools like `curl` or Postman.

---

## Setup

The application consists of separate **frontend** and **backend** components, which need to be started independently.

> **Note for Windows users**: Ensure Docker Desktop with WSL integration is installed and virtualization is enabled.

To launch the applications:

-   **Backend**

    ```bash
    cd backend && docker-compose up --build
    ```

-   **Frontend**
    ```bash
    cd frontend && npm start
    ```

**Ports in use:**

-   React (frontend): `http://localhost:3000`
-   Spring Boot (API): `http://localhost:8080`
-   Redis: `localhost:6379`

---

## Usage

### Managing Sanctioned Names

1. Navigate to the **"People"** page using the top navigation bar.
2. Add new names to the sanctioned list.
3. All names are displayed in a table.
4. You can:
    - **Edit** a name by double-clicking it, modifying the value, and pressing Enter.
    - **Delete** a name by clicking the trash-bin icon.

![People Page](https://github.com/user-attachments/assets/1ff6ecac-229f-470b-86e5-184340494e21)

---

### Verifying Names

1. Go to the **Homepage**.
2. Enter a name to verify — for example, a **misspelled version** of a previously added name. ![image](https://github.com/user-attachments/assets/fad9dc21-adf1-483b-a009-0b9f19ca9c50)
3. Click **Check**.
4. A popup will display the **most similar sanctioned name**, along with similarity scores for:
    - Jaro-Winkler
    - Jaccard
    - Levenshtein (normalized)
    - Phonetic matches

![Verification Alert](https://github.com/user-attachments/assets/e847bde4-b5fc-4b00-a91c-4c553c0ab91d)

If the input name does not sufficiently match any entry in the database, the alert will display:

```
Verification result: Name not found in sanctioned list
```

### Tests

Before launching tests, make sure Redis and Spring Boot services are up and running. To run all backend tests:

```bash
cd backend && ./mvnw test
```

---

## Notes

-   All names are **preprocessed** (lowercased, cleaned, tokenized, stop words removed) before similarity comparisons.
-   Matching thresholds are carefully chosen to balance precision and recall, but can be fine-tuned in the backend logic.
