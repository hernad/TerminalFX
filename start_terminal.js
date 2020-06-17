import {lib, hterm} from './libapps/hterm/dist/js/hterm_all.js'


function initContent(io) {
    const ver = lib.resource.getData('libdot/changelog/version');
    const date = lib.resource.getData('libdot/changelog/date');
    const pkg = `libdot ${ver} (${date})`;
    io.println("\r\n\
                              .--~~~~~~~~~~~~~------.\r\n\
                             /--===============------\\\r\n\
                             | |```````````````|     |\r\n\
                             | |               |     |\r\n\
                             | |      >_<      |     |\r\n\
                             | |               |     |\r\n\
                             | |_______________|     |\r\n\
                             |                   ::::|\r\n\
                             '======================='\r\n\
                             //-'-'-'-'-'-'-'-'-'-'-\\\\\r\n\
                            //_'_'_'_'_'_'_'_'_'_'_'_\\\\\r\n\
                            [-------------------------]\r\n\
                            \\_________________________/\r\n\
  \r\n\
                                 Welcome to hterm!\r\n\
                  Press F11 to go fullscreen to use all shortcuts.\r\n\
                         Running " + pkg + ".\r\n\
  ");
};

  
function updatePrefs(prefs) {
    var prefsObject = JSON.parse(prefs);
    for (var key in prefsObject) {
        var value = prefsObject[key];
        if (window.t) {
            term.getPrefs().set(key, value);
        }
    }
}

export function startTerminal() {

    //console.log("start terminal - fill terminal container");

    //console.log("typeof startTerminal: " + (typeof startTerminal));
    console.log("typeof hterm: " + (typeof hterm));
    console.log("typeof lib: " + (typeof lib));

    const ver = lib.resource.getData('libdot/changelog/version');
    const date = lib.resource.getData('libdot/changelog/date');
    console.log(`libdot ${ver} (${date})`);

    hterm.defaultStorage = new lib.Storage.Memory();
    //console.log("2");
    hterm.copySelectionToClipboard = function (document) {
        console.copy(document.getSelection().toString());
    };
    hterm.msg = function (name, args = [], string) {
        return string;
    };
    
    var term = new hterm.Terminal("myterm");
    console.log("3");
   
    updatePrefs(console.getPrefs());

    //console.log("3-b");
    term.onTerminalReady = function () {

        //console.log("5-1");
        console.onTerminalInit();

        var io = term.io.push();

        io.onVTKeystroke = function (str) {
            //console.log("5-2");
            console.command(str);
            //console.log("5-3");
        };

        //console.log("5-4");
        io.sendString = io.onVTKeystroke;

        io.onTerminalResize = function (columns, rows) {
            //console.log("5-5");
            console.resizeTerminal(columns, rows);
        };

        //console.log("5-6");
        //console.log("5-7");
        console.onTerminalReady();
        //console.log("5-8");

    };
    console.log("4");

    var element = document.querySelector('#terminal');
    //console.log("type of element:" + (typeof element));
    //console.log("type of term.decorate:" + (typeof term.decorate));
    
    //element.innerHTML = "ja sam terminal";
    term.decorate(element);
    term.installKeyboard();

    window.t = term;
    //console.log("5");

    //console.log("end startTerminal");
}

//console.warn("test warn");
//console.log("bundleeeeeeeeeeeeee");

document.addEventListener("DOMContentLoaded", function (event) {
    lib.init(startTerminal);
});
