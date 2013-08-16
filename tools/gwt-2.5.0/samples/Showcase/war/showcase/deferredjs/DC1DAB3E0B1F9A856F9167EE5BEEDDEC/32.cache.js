function sD(){sD=Nmc;rD=new qkc}
function tD(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.qd(c)}}
function ZTb(a,b,c,d){var e;a.a._f(b,c);e=_Tb(a.a.i,b,c);dj(e,d,true)}
function xD(){sD();var a;a=DH(rD.ld(Byc),60);if(!a){a=new wD;rD.nd(Byc,a)}return a}
function uD(c,b){try{typeof $wnd[b]!='object'&&zD(b);c.a=$wnd[b]}catch(a){zD(b)}}
function zD(a){throw new rlc(trc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function vD(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.gd(a);return String(c)}
function wD(){this.b='Dictionary userInfo';uD(this,Byc);if(!this.a){throw new rlc("Cannot find JavaScript object with the name 'userInfo'")}}
function Rjb(){var a,b,c,d,e,f,g,i,j,k,n;f=new N4b;g=new oRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=Pqc;g.cb.style['textAlign']=grc;K4b(f,new oRb('<b>This example interacts with the following JavaScript variable:<\/b>'));K4b(f,g);j=new RTb;b=j.j;i=xD();e=(n=new ykc,tD(i,n),n);a=0;for(d=uhc(yE(e.a));d.a.wd();){c=DH(Ahc(d),1);k=vD(i,c);ITb(j,0,a,c);ZTb(b,0,a,'cw-DictionaryExample-header');ITb(j,1,a,k);ZTb(b,1,a,'cw-DictionaryExample-data');++a}K4b(f,new oRb('<br><br>'));K4b(f,j);return f}
var Byc='userInfo';o1(343,1,{60:1},wD);_.gd=function yD(a){var b;b="Cannot find '"+a+"' in "+this;throw new rlc(b)};_.tS=function AD(){return this.b};_.a=null;_.b=null;var rD;o1(641,1,Hnc);_.lc=function Xjb(){Y3(this.a,Rjb())};var OM=Xbc(Cuc,csc,343);uoc(wn)(32);