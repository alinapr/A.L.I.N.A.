var data1 = [ ];
var labels = [ ];
var labelscounter=0;

var t = setInterval(function updateChart() {

var client = new XMLHttpRequest();
    client.open('GET', "compressionRate.txt?"+Math.random());
    client.onreadystatechange = function() {

      if (client.responseText) {

          data1.push(client.responseText); 
          console.log(client.responseText); 
          if (labelscounter>9){
            data1.splice(0, 1);
            labels.splice(0, 1);
          }

          labels.push(labelscounter);
          labelscounter++;

      }

     }
    client.send();
}, 1000); 

function printDiagrams() {
    var ctx = document.getElementById("lineChart").getContext('2d');


    var myChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
        label: 'Rate',
        data: data1,
        fill: false, 
        backgroundColor: "rgba(100,255,51,1)"
        }]
      }, 

      options:{
          animation:false,
          scales: {
            yAxes: [{
              ticks: {
                min: 100,  
                max: 140 
              }
            }]
          }
        }
      });
}
