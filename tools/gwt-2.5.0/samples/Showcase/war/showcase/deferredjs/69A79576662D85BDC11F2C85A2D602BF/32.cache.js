function $D(){$D=ync;ZD=new blc}
function CUb(a,b,c,d){var e;a.b.hg(b,c);e=EUb(a.b.j,b,c);pj(e,d,true)}
function _D(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.yd(c)}}
function bE(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.pd(a);return String(c)}
function dE(){$D();var a;a=jI(ZD.td(yzc),60);if(!a){a=new cE;ZD.vd(yzc,a)}return a}
function aE(c,b){try{typeof $wnd[b]!='object'&&fE(b);c.b=$wnd[b]}catch(a){fE(b)}}
function fE(a){throw new cmc(osc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function cE(){this.c='Dictionary userInfo';aE(this,yzc);if(!this.b){throw new cmc("Cannot find JavaScript object with the name 'userInfo'")}}
function Akb(){var a,b,c,d,e,f,g,i,j,k,n;f=new p5b;g=new TRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=Orc;g.db.style['textAlign']=bsc;m5b(f,new TRb('<b>This example interacts with the following JavaScript variable:<\/b>'));m5b(f,g);j=new uUb;b=j.k;i=dE();e=(n=new jlc,_D(i,n),n);a=0;for(d=fic(eF(e.b));d.b.Ed();){c=jI(lic(d),1);k=bE(i,c);lUb(j,0,a,c);CUb(b,0,a,'cw-DictionaryExample-header');lUb(j,1,a,k);CUb(b,1,a,'cw-DictionaryExample-data');++a}m5b(f,new TRb('<br><br>'));m5b(f,j);return f}
var yzc='userInfo';c2(346,1,{60:1},cE);_.pd=function eE(a){var b;b="Cannot find '"+a+"' in "+this;throw new cmc(b)};_.tS=function gE(){return this.c};_.b=null;_.c=null;var ZD;c2(643,1,toc);_.qc=function Gkb(){H4(this.b,Akb())};var CN=Hcc(yvc,Zsc,346);gpc(Jn)(32);