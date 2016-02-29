/* Installation
 *
 * The following will install the necessary npm modules for this gulpfile:
 *

 % npm install gulp gulp-ruby-sass gulp-autoprefixer gulp-minify-css \
    gulp-jshint gulp-concat gulp-uglify gulp-imagemin gulp-notify gulp-rename \
    del gulp-util gulp-html-replace es6-promise --save-dev

 *
 */


// Load plugins
var gulp = require('gulp');
var autoprefixer = require('gulp-autoprefixer');
var minifycss = require('gulp-minify-css');
var jshint = require('gulp-jshint');
var uglify = require('gulp-uglify');
var imagemin = require('gulp-imagemin');
var rename = require('gulp-rename');
var concat = require('gulp-concat');
var notify = require('gulp-notify');
var htmlreplace = require('gulp-html-replace');
var del = require('del');

var Promise = require('es6-promise').Promise;

// Source path
var srcPath = 'ear/default/web/';

// Distribution path
var distPath = 'build/default/web/';


// Styles
gulp.task('styles', function() {
  return gulp.src([srcPath + 'bower_components/angular/angular-csp.css', srcPath + 'bower_components/angular-material/angular-material.css', srcPath + 'bower_components/angular-material/default-theme.css', srcPath + 'css/*.css'])
    .pipe(autoprefixer({browsers: ['last 2 versions'], cascade: false}))
    .pipe(concat('styles.css'))
    .pipe(gulp.dest(distPath + 'css'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(minifycss())
    .pipe(gulp.dest(distPath + 'css'))
    .pipe(notify({
      message: 'Styles task complete'
    }));
});


// Custom scripts
gulp.task('scripts', function() {
  return gulp.src([srcPath + 'js/*.js'])
    .pipe(concat('main.js'))
    .pipe(gulp.dest(distPath + 'js'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(uglify())
    .pipe(gulp.dest(distPath + 'js'))
    .pipe(notify({
      message: 'Custom Scripts task complete'
    }));
});


// Library scripts
gulp.task('ng-scripts', function() {
  return gulp.src([srcPath + 'bower_components/angular/angular.js', srcPath + 'bower_components/angular-route/angular-route.js', srcPath + 'bower_components/angular-aria/angular-aria.js', srcPath + 'bower_components/angular-animate/angular-animate.js', srcPath + 'bower_components/angular-material/angular-material.js', srcPath + 'bower_components/ace-builds/src-min-noconflict/ace.js', srcPath + 'bower_components/angular-ui-ace/ui-ace.js', srcPath + 'eod/peg-0.8.0.min.js', srcPath + 'eod/conditional_parser.js'])
    .pipe(concat('angular.js'))
    .pipe(gulp.dest(distPath + 'js'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(uglify())
    .pipe(gulp.dest(distPath + 'js'))
    .pipe(notify({
      message: 'Library Scripts task complete'
    }));
});


// Images
gulp.task('images', function() {
  return gulp.src(srcPath + 'img/*')
    .pipe(imagemin({
      optimizationLevel: 3,
      progressive: true,
      interlaced: true
    }))
    .pipe(gulp.dest(distPath + 'img'))
    .pipe(notify({
      message: 'Images task complete'
    }));
});


// Copy partials
gulp.task('partials', function() {
  gulp.src(srcPath + 'partials/*')
    .pipe(gulp.dest(distPath + 'partials'));
});


// Copy index.html
gulp.task('index', function() {
  gulp.src(srcPath + 'gulp.html')
    .pipe(rename('index.html'))
    .pipe(gulp.dest(distPath));
});


// Copy favicon
gulp.task('favicon', function() {
  gulp.src(srcPath + 'favicon.ico')
    .pipe(gulp.dest(distPath));
});


// Clean
gulp.task('clean', function(cb) {
  del([distPath], {force: true}, cb);
});


// Default task
gulp.task('default', [], function() {
  gulp.start('styles', 'scripts', 'ng-scripts', 'images', 'partials', 'favicon', 'index');
});
