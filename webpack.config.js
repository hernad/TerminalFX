const path = require("path");
const webpack = require("webpack");
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {

  entry: {
    path: path.resolve(__dirname, 'start_terminal.js')
  },
  //mode: "production",
  //devtool: "source-map",
  mode: "production",
  output: {
    path: path.resolve(__dirname, "src", "main", "resources"),
    filename: "bundle.js"
  },
  optimization: {
    minimize: true
  },
  plugins: [new HtmlWebpackPlugin()]
};

/*
module.exports = {
  
  entry: 
  output: {
    path: path.resolve(__dirname, "dist")
  }
};
*/
