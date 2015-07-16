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


// Distribution path
var distPath = '../ear/default/dist/'


// Styles
gulp.task('styles', function() {
  return gulp.src(['bower_components/angular/angular-csp.css', 'bower_components/angular-material/angular-material.css', 'bower_components/angular-material/default-theme.css', 'css/*.css'])
    .pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'))
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
  return gulp.src(['js/*.js'])
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
  return gulp.src(['bower_components/angular/angular.js', 'bower_components/angular-route/angular-route.js', 'bower_components/angular-aria/angular-aria.js', 'bower_components/angular-animate/angular-animate.js', 'bower_components/angular-material/angular-material.js', 'bower_components/ace-builds/src-min-noconflict/ace.js', 'bower_components/angular-ui-ace/ui-ace.js', 'eod/peg-0.8.0.min.js', 'eod/conditional_parser.js'])
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
  return gulp.src('img/*')
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
  gulp.src('partials/*')
    .pipe(gulp.dest(distPath + 'partials'));
});


// Copy index.html
gulp.task('indexhtml', function() {
  gulp.src('gulp.html')
    .pipe(rename('index.html'))
    .pipe(gulp.dest(distPath));
});


// Copy favicon
gulp.task('favicon', function() {
  gulp.src('favicon.ico')
    .pipe(gulp.dest(distPath));
});


// Clean
gulp.task('clean', function(cb) {
  del([distPath], {force: true}, cb);
});


// Default task
gulp.task('default', ['clean'], function() {
  gulp.start('styles', 'scripts', 'ng-scripts', 'images', 'partials', 'favicon', 'indexhtml');
});
