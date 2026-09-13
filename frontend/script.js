// Selected mood store करण्यासाठी
let selectedMood = "";

// User selected travel preferences
let selectedPreferences = [];


// --------------------------------
// PREFERENCE SELECT / UNSELECT
// --------------------------------

function togglePreference(card, preference) {

    // जर preference आधीच selected असेल
    if (selectedPreferences.includes(preference)) {

        // Array मधून remove करा
        selectedPreferences =
            selectedPreferences.filter(function(item) {
                return item !== preference;
            });

        // Card ची selection remove करा
        card.classList.remove("selected");

    } else {

        // Preference add करा
        selectedPreferences.push(preference);

        // Card select करा
        card.classList.add("selected");
    }

    console.log(
        "Selected Preferences:",
        selectedPreferences
    );
}


// --------------------------------
// MOOD SELECT
// --------------------------------

function selectMood(mood, card) {

    selectedMood = mood;

    // सर्व mood cards शोधणे
    const cards =
        document.querySelectorAll(".mood-card");

    // आधीची selection remove करणे
    cards.forEach(function(item) {
        item.classList.remove("selected");
    });

    // User ने click केलेला card select करणे
    card.classList.add("selected");

    console.log(
        "Selected Mood:",
        selectedMood
    );
}


// --------------------------------
// GENERATE TRIP
// --------------------------------

function generateTrip() {

    const budget =
        document.getElementById("budget").value;

    const days =
        document.getElementById("days").value;

    const location =
        document.getElementById("location").value;


    // --------------------------------
    // MOOD CHECK
    // --------------------------------

    if (selectedMood === "") {

        alert(
            "Please select your mood first ❤️"
        );

        return;
    }


    // --------------------------------
    // INPUT CHECK
    // --------------------------------

    if (
        budget === "" ||
        days === "" ||
        location === ""
    ) {

        alert(
            "Please enter Budget, Days and Starting Location."
        );

        return;
    }


    // --------------------------------
    // BACKEND API URL
    // --------------------------------

    const preferencesText =
        selectedPreferences.join(",");

    const apiUrl =
        `https://sahayatri-ai-trip-planner.onrender.com/api/trip` +
        `?location=${encodeURIComponent(location)}` +
        `&days=${days}` +
        `&budget=${budget}` +
        `&mood=${encodeURIComponent(selectedMood)}` +
        `&preferences=${encodeURIComponent(preferencesText)}`;


    console.log(
        "Sending request to:",
        apiUrl
    );


    // --------------------------------
    // SEND REQUEST TO BACKEND
    // --------------------------------

    fetch(apiUrl)

        .then(response => {

            if (!response.ok) {

                throw new Error(
                    "Backend API error"
                );
            }

            // JSON response घेणे
            return response.json();
        })


        // --------------------------------
        // BACKEND RESPONSE
        // --------------------------------

        .then(data => {

            console.log(
                "Backend Response:",
                data
            );


            // --------------------------------
            // SAVE TRIP INFORMATION
            // --------------------------------

            localStorage.setItem(
                "tripMood",
                selectedMood
            );


            localStorage.setItem(
                "tripBudget",
                budget
            );


            localStorage.setItem(
                "tripDays",
                days
            );


            localStorage.setItem(
                "tripLocation",
                location
            );


            // --------------------------------
            // SAVE PREFERENCES
            // --------------------------------

            localStorage.setItem(
                "tripPreferences",
                JSON.stringify(
                    selectedPreferences
                )
            );


            // --------------------------------
            // SAVE BACKEND RESPONSE
            // --------------------------------

            localStorage.setItem(
                "tripResponse",
                JSON.stringify(data)
            );


            console.log(
                "Trip saved successfully!"
            );


            // --------------------------------
            // GO TO DASHBOARD
            // --------------------------------

            window.location.href =
                "dashboard.html";

        })


        // --------------------------------
        // ERROR HANDLING
        // --------------------------------

        .catch(error => {

            console.error(
                "Error:",
                error
            );

            alert(
                "Unable to connect to Sahayatri server. " +
                "Please make sure the backend is running."
            );

        });
}


// --------------------------------
// AUTO-FILL LOCATION FROM REEL SCANNER
// --------------------------------

const urlParams =
    new URLSearchParams(window.location.search);

const detectedLocation =
    urlParams.get("location");

if (detectedLocation) {

    const locationInput =
        document.getElementById("location");

    if (locationInput) {

        locationInput.value =
            detectedLocation;

        console.log(
            "Detected Reel Location:",
            detectedLocation
        );
    }
}