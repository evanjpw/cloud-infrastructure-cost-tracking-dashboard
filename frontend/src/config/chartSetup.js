// Chart.js setup and registration of all components
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';

// Register all Chart.js components
ChartJS.register(
  CategoryScale,    // For category (x-axis) scales
  LinearScale,      // For linear (y-axis) scales  
  PointElement,     // For points in line charts
  LineElement,      // For lines in line charts
  BarElement,       // For bars in bar charts
  ArcElement,       // For arcs in pie/doughnut charts
  Title,            // For chart titles
  Tooltip,          // For hover tooltips
  Legend,           // For chart legends
  Filler            // For filled areas under lines
);

export default ChartJS;