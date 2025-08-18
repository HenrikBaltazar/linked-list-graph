const { invoke } = window.__TAURI__.core;

let canvas, ctx;
let graphData = null;

async function loadGraph() {
  graphData = await invoke("get_graph");
  resizeCanvas(); // já desenha no tamanho certo
}

function resizeCanvas() {
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  if (graphData) {
    drawGraph(graphData.adj);
  }
}

function drawGraph(adj) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  const nodes = Object.keys(adj);
  const radius = Math.min(canvas.width, canvas.height) / 2.5; // ajusta ao tamanho da tela
  const centerX = canvas.width / 2;
  const centerY = canvas.height / 2;

  // calcular posições em círculo
  const positions = {};
  nodes.forEach((node, i) => {
    const angle = (2 * Math.PI * i) / nodes.length;
    positions[node] = {
      x: centerX + radius * Math.cos(angle),
      y: centerY + radius * Math.sin(angle),
    };
  });

  // desenhar arestas
  ctx.strokeStyle = "#555";
  ctx.lineWidth = 2;
  for (const [node, neighbors] of Object.entries(adj)) {
    neighbors.forEach((neighbor) => {
      const { x: x1, y: y1 } = positions[node];
      const { x: x2, y: y2 } = positions[neighbor];
      ctx.beginPath();
      ctx.moveTo(x1, y1);
      ctx.lineTo(x2, y2);
      ctx.stroke();
    });
  }

  // desenhar nós
  for (const [node, pos] of Object.entries(positions)) {
    ctx.beginPath();
    ctx.arc(pos.x, pos.y, 20, 0, 2 * Math.PI);
    ctx.fillStyle = "#3498db";
    ctx.fill();
    ctx.strokeStyle = "#2c3e50";
    ctx.stroke();

    // label
    ctx.fillStyle = "#fff";
    ctx.font = "16px Arial";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(node, pos.x, pos.y);
  }
}

window.addEventListener("DOMContentLoaded", () => {
  canvas = document.querySelector("#graph-canvas");
  ctx = canvas.getContext("2d");
  loadGraph();
});

// quando a janela mudar de tamanho
window.addEventListener("resize", resizeCanvas);
