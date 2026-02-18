const events = document.querySelector("#events");
const failingEvents = document.querySelector("#failingEvents");

let evtSource;

function createConnection() {
    evtSource = new EventSource("/api/sse/stream");

    evtSource.onopen = () => {
        console.log("Connection established");
        console.log(evtSource);
    };

    evtSource.addEventListener("custom", (event) => {
        console.log("Received CUSTOM event: ", event)
        const li = document.createElement("li")
        li.innerHTML = "CUSTOM:" + event.data
        events.append(li)
    })

    evtSource.addEventListener("done", (event) => {
        console.log("Received DONE event: ", event)
        const li = document.createElement("li")
        li.innerHTML = "DONE:" + event.data
        events.append(li)
    })

    evtSource.addEventListener("failed", (event) => {
        console.log("Received FAILED event: ", event)
        const li = document.createElement("li")
        li.innerHTML = "FAILED:" + event.data
        events.append(li)
    })

    evtSource.onmessage = (event) => {
        console.log(event);
        console.log(evtSource.readyState) //0 - connecting, 1 - connected, 2 - permanently closed
        const li = document.createElement("li")
        li.innerHTML = event.data
        events.append(li)
    };

    evtSource.onerror = (event) => {
        console.log("Connection closed", event)
        console.log(evtSource.readyState)
        evtSource.close();
    }
}

function resetConnection() {
    if (evtSource) {
        evtSource.close();
    }

    alert("Resetting the connection!");
    events.innerHTML = "";
    createConnection();
}


let failingEvtSource;

function createFailingConnection() {
    failingEvtSource = new EventSource("/api/sse/failing-stream");

    failingEvtSource.onopen = () => {
        console.log("Failing Connection established");
        console.log(failingEvtSource);
    };

    failingEvtSource.addEventListener("custom", (event) => {
        console.log("Received CUSTOM event: ", event)
        const li = document.createElement("li")
        li.innerHTML = "CUSTOM:" + event.data
        failingEvents.append(li)
    })

    failingEvtSource.addEventListener("done", (event) => {
        //Here we can check if there is everything ok
        console.log("Received DONE event: ", event)
        const li = document.createElement("li")
        li.innerHTML = "DONE:" + event.data
        failingEvents.append(li)
    })

    failingEvtSource.addEventListener("failed", (event) => {
        //Here we can check if there was an error
        console.log("Received FAILED event: ", event)
        const li = document.createElement("li")
        li.innerHTML = "FAILED:" + event.data
        failingEvents.append(li)
    })

    failingEvtSource.onmessage = (event) => {
        console.log(event);
        console.log(failingEvtSource.readyState) //0 - connecting, 1 - connected, 2 - permanently closed
        const li = document.createElement("li")
        li.innerHTML = event.data
        failingEvents.append(li)
    };

    failingEvtSource.onerror = (event) => {
        //Here we can not check whether there was an error or everything is ok. Once we emit emitter.complete() this will be triggered as well.
        console.log("Failed Connection closed", event)
        console.log(failingEvtSource.readyState)
        failingEvtSource.close();
    }
}

function resetFailingConnection() {
    if (failingEvtSource) {
        failingEvtSource.close();
    }

    alert("Resetting the failing connection!");
    failingEvents.innerHTML = "";
    createFailingConnection();
}



const resetBtn = document.querySelector("#resetConnection");
resetBtn.addEventListener("click", resetConnection);
const resetFailingBtn = document.querySelector("#resetFailingConnection");
resetFailingBtn.addEventListener("click", resetFailingConnection);