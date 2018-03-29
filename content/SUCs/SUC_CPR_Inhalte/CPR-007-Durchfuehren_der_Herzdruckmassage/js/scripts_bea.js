/*function readTextFile(file) {
    var rawFile = new XMLHttpRequest();
    rawFile.open("GET", file, false);
    rawFile.onreadystatechange = function ()
    {
        if(rawFile.readyState === 4)
        {
            if(rawFile.status === 200 || rawFile.status == 0)
            {
                var allText = rawFile.responseText;
                console.log(allText);
            }
        }
    }
    rawFile.send(null);
}

setInterval(readTextFile("test.txt"), 2000); 
*/

setInterval(function() {var client = new XMLHttpRequest();
    client.open('GET', "breathperminute.txt?"+Math.random());
    client.onreadystatechange = function() {
    //    console.log(client.responseText);
        document.getElementById('value').innerText=client.responseText;
    }
    client.send();}, 500); 
//var a = 1; 
//setInterval(function(){ console.log(a++); }, 3000);
