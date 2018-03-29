var data2 = [ ];
var labels2 = [ ];
var labelscounter2=0;

var t = setInterval(function updateChart() {

var client2 = new XMLHttpRequest();
    client2.open('GET', "compressionDepth.txt?"+Math.random());
    client2.onreadystatechange = function() {

      if (client2.responseText) {

          data2.push(client2.responseText); 
          console.log(client2.responseText); 
          var ctx = document.getElementById("barChart").getContext('2d');
          if (labelscounter2>9){
            data2.splice(0, 1);
            labels2.splice(0, 1);
          }

          labels2.push(labelscounter2);
          labelscounter2++;
          var myChart2 = new Chart(ctx, {
            type: 'bar',
            data: {
              labels: labels2,
              datasets: [{
              label: 'Depth',
              data: data2,
              backgroundColor: "rgba(153,255,51,1)"
              }]
            },
            options:{
              animation:false,
              scales: {
                yAxes: [{
                  ticks: {
                    beginAtZero: true, 
                    max: 100 
                  }
                }]
              }
            }
          });
      }

     }
    client2.send();
}, 1000); 
