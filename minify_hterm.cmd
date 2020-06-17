REM copy  src\main\resources\hterm_all_2020.js

node_modules\.bin\minify  --js libapps\hterm\dist\js\hterm_all.js > src/main/resources/hterm_all_2020.min.js
