const otp = document.getElementById("otp")
const otpForm = document.getElementById("otpForm")
const otpInput = document.getElementById("otpInput")
const generateOtpBtn = document.getElementById("generateOtp")
const validateOtpBtn = document.getElementById("validateOtp")

otpForm.addEventListener("submit", (event) => {
    event.preventDefault();

    const formData = new FormData(otpForm);

    fetch("/api/otp/validate", {
        method: "POST",
        body: formData
    })
        .then(response => response.status)
        .then(status => console.log(status))
})

let evtSource;

generateOtpBtn.addEventListener("click", () => {
    console.log("Generating new OTP code")
    fetch("/api/otp/generate", {
        method: "POST",
    })
        // .then(response => response.json())
        // .then(json => console.log(json))
        .then(response => response.status)
        .then(status => {
            console.log(status)
            if (status === 200) {
                otp.innerHTML = "NOT VALIDATED"
            }

            evtSource = new EventSource("/api/otp/stream");

            evtSource.onopen = (event) => {
                console.log("Connection established");
                console.log(event);
            };

            evtSource.addEventListener("valid", (event) => {
                console.log("Received VALID event: ", event)
                otp.innerHTML = "VALID"
            })

            evtSource.addEventListener("invalid", (event) => {
                console.log("Received INVALID event: ", event)
                otp.innerHTML = "INVALID"
            })

            evtSource.addEventListener("expired", (event) => {
                console.log("Received EXPIRED event: ", event)
                otp.innerHTML = "EXPIRED"
            })

            evtSource.onerror = (event) => {
                console.log("Connection closed", event)
                console.log(evtSource.readyState)
                evtSource.close();
            }
        });
})
