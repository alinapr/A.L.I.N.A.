var ongoing_rea = false; 
var seconds = 0; 
var minutes = 0; 
var hours = 0; 
var t; 
var z; 

var z_seconds = 0; 
var z_minutes = 2;
var anzahl_zyklus = 0;  

var zyk_time; 
var time; 
var anz_zyk; 

function start_stopp_rea() {

    time = document.getElementById("total_time"); 
    anz_zyk = document.getElementById("anz_zyklus"); 
    zyk_time = document.getElementById("countdown"); 

    if (ongoing_rea == false) {
        timer(); 
        count_down();
        anz_zyk.textContent = ++anzahl_zyklus + ".";
    }
    
    var elem = document.getElementById("but_ss_rea"); 
    if (ongoing_rea == false) {
    //    timer; 
        console.log("Reanimation gestartet");
        elem.firstChild.data = "Stopp";
        ongoing_rea = true; 
    } else {
        clearTimeout(t); 
        clearTimeout(z); 
        console.log("Reanimation beendet"); 
        elem.firstChild.data = "Start";  
        ongoing_rea = false; 
    }
}

function count_down() {
    if (z_seconds <= 0) {
        z_seconds = 60; 
        z_minutes -= 1; 
    } if (z_minutes <= -1) {
        z_seconds = 0; 
        z_minutes += 1; 
    } else {
        z_seconds -= 1; 
        if (z_seconds < 10 && z_seconds >= 0) {
            zyk_time.textContent = "0" + z_minutes + ":0" + z_seconds;
        } else {
            zyk_time.textContent = "0" + z_minutes + ":" + z_seconds; 
        }
        z = setTimeout(count_down, 1000); 
    }
}

function timer() {
    t = setTimeout(add, 1000); 
}

function add() {
    seconds++; 
    if (seconds >= 60) {
        seconds = 0;
        minutes++;
        if (minutes >= 60) {
            minutes = 0;
            hours++;
        }
    }
    time.textContent = (hours ? (hours > 9 ? hours : "0" + hours) : "00") + ":" + (minutes ? (minutes > 9 ? minutes : "0" + minutes) : "00") + ":" + (seconds > 9 ? seconds : "0" + seconds);
    timer(); 
}

function clear_stopwatch() {
    var time = document.getElementById("total_time"); 
    time.textContent = "00:00:00"; 
    seconds = 0; 
    minutes = 0; 
    hours = 0; 
}

function but_zyk_reset() {
    var zyk_time = document.getElementById("countdown");
    var anz_zyklus = document.getElementById("anz_zyklus"); 
    zyk_time.textContent = "02:00"; 
    anz_zyklus.textContent = ++anzahl_zyklus + "."; 
    z_seconds = 60; 
    z_minutes = 1; 
    count_down(); 
}
