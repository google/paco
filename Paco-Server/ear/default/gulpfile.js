// Load plugins
var gulp = require('gulp'),
  autoprefixer = require('gulp-autoprefixer'),
  minifycss = require('gulp-minify-css'),
  jshint = require('gulp-jshint'),
  uglify = require('gulp-uglify'),
  imagemin = require('gulp-imagemin'),
  rename = require('gulp-rename'),
  concat = require('gulp-concat'),
  notify = require('gulp-notify'),
  cache = require('gulp-cache'),
  livereload = require('gulp-livereload'),
  htmlreplace = require('gulp-html-replace'),
  del = require('del');

// Styles
gulp.task('styles', function() {
  return gulp.src(['bower_components/angular/angular-csp.css', 'bower_components/angular-material/angular-material.css', 'bower_components/angular-material/default-theme.css', 'css/*.css'])
    .pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'))
    .pipe(concat('styles.css'))
    .pipe(gulp.dest('dist'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(minifycss())
    .pipe(gulp.dest('dist'))
    .pipe(notify({
      message: 'Styles task complete'
    }));
});

// Scripts
gulp.task('scripts', function() {
  return gulp.src(['js/*.js'])
    .pipe(concat('main.js'))
    .pipe(gulp.dest('dist'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(uglify())
    .pipe(gulp.dest('dist'))
    .pipe(notify({
      message: 'Scripts task complete'
    }));
});

// Scripts
gulp.task('ng-scripts', function() {
  return gulp.src(['bower_components/angular/angular.js', 'bower_components/angular-route/angular-route.js', 'bower_components/angular-aria/angular-aria.js', 'bower_components/angular-animate/angular-animate.js', 'bower_components/angular-material/angular-material.js'])
    .pipe(concat('angular.js'))
    .pipe(gulp.dest('dist'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(uglify())
    .pipe(gulp.dest('dist'))
    .pipe(notify({
      message: 'Angular Scripts task complete'
    }));
});


// Images
gulp.task('images', function() {
  return gulp.src('img/*')
    .pipe(cache(imagemin({
      optimizationLevel: 3,
      progressive: true,
      interlaced: true
    })))
    .pipe(gulp.dest('dist/img'))
    .pipe(notify({
      message: 'Images task complete'
    }));
});

// Copy fonts from a module outside of our project (like Bower)
gulp.task('copyfiles', function() {
  gulp.src('partials/*')
    .pipe(gulp.dest('dist/partials'));

  gulp.src('js/*json')
    .pipe(gulp.dest('dist/js'));


});

gulp.task('indexhtml', function() {
  gulp.src('gulp.html')
    .pipe(rename('index.html'))
    .pipe(gulp.dest('dist/'));
});


// Clean
gulp.task('clean', function(cb) {
  del(['dist/assets/css', 'dist/assets/js', 'dist/assets/img', 'Content/Sass/.sass-cache'], cb);
});

// Default task
gulp.task('default', ['clean'], function() {
  gulp.start('styles', 'scripts', 'ng-scripts', 'images', 'copyfiles', 'indexhtml');
});
