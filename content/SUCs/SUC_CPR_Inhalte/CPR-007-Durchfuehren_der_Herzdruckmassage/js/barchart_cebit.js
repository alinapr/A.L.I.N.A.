var data2 = [ ];
var labels2 = [ ];
var labelscounter2=0;

var data1 = [ ];
var labels1 = [ ];
var labelscounter1=0;

var t = setInterval(function updateChart() {

var client2 = new XMLHttpRequest();
    client2.open('GET', "compressionDepth.txt?"+Math.random());
    client2.onreadystatechange = function() {

      if (client2.responseText) {

          data2.push(client2.responseText); 
          console.log(client2.responseText); 
          if (labelscounter2>9){
            data2.splice(0, 1);
            labels2.splice(0, 1);
          }

          labels2.push(labelscounter2);
          labelscounter2++;
      }
    }
    client2.send();

    var client = new XMLHttpRequest();
    client.open('GET', "compressionRate.txt?"+Math.random());
    client.onreadystatechange = function() {

      if (client.responseText) {

          data1.push(client.responseText); 
          console.log(client.responseText); 
          if (labelscounter1>9){
            data1.splice(0, 1);
            labels1.splice(0, 1);
          }

          labels1.push(labelscounter1);
          labelscounter1++;
        }
      }
      client.send();

  printDiagrams(); 
}, 1000); 

function printDiagrams() {

  var ctx = document.getElementById("barChart").getContext('2d');

  var myChart2 = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels2,
      datasets: [
      {
        type: 'line',
        label: 'Compression Rate',
        backgroundColor: "rgba(0, 102, 204, 1)", 
        borderColor: "rgba(0, 102, 204, 1)", 
        fill: false,
        data: data1,
      },
      {
        type: 'bar',
        label: 'Compression Depth',
        backgroundColor: "rgba(0, 153, 0, 1)", 
        borderColor: "rgba(0, 153, 0, 1)", 
        data: data2,
      }
      ]
      },
      options:{
        animation:false,
        scales: {
        yAxes: [{
        ticks: {
        min: 0, 
        max: 140 
      }
      }]
    }
  }
  });
}
