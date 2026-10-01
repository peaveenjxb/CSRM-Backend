const express = require("express");

const app = express();
app.use(express.json());

const port = process.env.PORT || 5000;

app.get("/", (_req, res) => {
  res.send("CSRM Backend is running");
});

app.listen(port, "0.0.0.0", () => {
  console.log(`Server running on port ${port}`);
});