function iE(){iE=Inc;hE=new llc}
function jE(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.td(c)}}
function MUb(a,b,c,d){var e;a.b.cg(b,c);e=OUb(a.b.j,b,c);pj(e,d,true)}
function nE(){iE();var a;a=pI(hE.od(Ezc),61);if(!a){a=new mE;hE.qd(Ezc,a)}return a}
function kE(c,b){try{typeof $wnd[b]!='object'&&pE(b);c.b=$wnd[b]}catch(a){pE(b)}}
function pE(a){throw new mmc(zsc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function lE(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.kd(a);return String(c)}
function mE(){this.c='Dictionary userInfo';kE(this,Ezc);if(!this.b){throw new mmc("Cannot find JavaScript object with the name 'userInfo'")}}
function Lkb(){var a,b,c,d,e,f,g,i,j,k,n;f=new z5b;g=new bSb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=Yrc;g.db.style['textAlign']=msc;w5b(f,new bSb('<b>\u8FD9\u4E2A\u4F8B\u5B50\u4F7F\u7528\u4E0B\u5217Javascript\u7684\u53D8\u91CF\uFF1A <\/b>'));w5b(f,g);j=new EUb;b=j.k;i=nE();e=(n=new tlc,jE(i,n),n);a=0;for(d=pic(oF(e.b));d.b.zd();){c=pI(vic(d),1);k=lE(i,c);vUb(j,0,a,c);MUb(b,0,a,'cw-DictionaryExample-header');vUb(j,1,a,k);MUb(b,1,a,'cw-DictionaryExample-data');++a}w5b(f,new bSb('<br><br>'));w5b(f,j);return f}
var Ezc='userInfo';n2(349,1,{61:1},mE);_.kd=function oE(a){var b;b="Cannot find '"+a+"' in "+this;throw new mmc(b)};_.tS=function qE(){return this.c};_.b=null;_.c=null;var hE;n2(647,1,Doc);_.qc=function Rkb(){S4(this.b,Lkb())};var MN=Rcc(Hvc,'Dictionary',349);qpc(Jn)(32);