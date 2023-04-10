const path = require('path');

module.exports = {
    mode: 'development',
    entry: ['./js/app.js', './js/factories/usersFactory.js'],
    output: {
        filename: 'main.bundle.js',
        path: path.resolve(__dirname, 'dist')
    },
    devtool: 'source-map'
};