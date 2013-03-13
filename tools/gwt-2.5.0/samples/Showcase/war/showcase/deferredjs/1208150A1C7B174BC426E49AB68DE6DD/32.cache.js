function XD(){XD=knc;WD=new Pkc}
function YD(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.pd(c)}}
function wUb(a,b,c,d){var e;a.b.$f(b,c);e=yUb(a.b.j,b,c);oj(e,d,true)}
function aE(){XD();var a;a=cI(WD.kd(gzc),61);if(!a){a=new _D;WD.md(gzc,a)}return a}
function ZD(c,b){try{typeof $wnd[b]!='object'&&cE(b);c.b=$wnd[b]}catch(a){cE(b)}}
function cE(a){throw new Qlc(Wrc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function $D(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.fd(a);return String(c)}
function _D(){this.c='Dictionary userInfo';ZD(this,gzc);if(!this.b){throw new Qlc("Cannot find JavaScript object with the name 'userInfo'")}}
function skb(){var a,b,c,d,e,f,g,i,j,k,n;f=new g5b;g=new NRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=prc;g.db.style['textAlign']=Jrc;d5b(f,new NRb('<b>\u8FD9\u4E2A\u4F8B\u5B50\u4F7F\u7528\u4E0B\u5217Javascript\u7684\u53D8\u91CF\uFF1A <\/b>'));d5b(f,g);j=new oUb;b=j.k;i=aE();e=(n=new Xkc,YD(i,n),n);a=0;for(d=Thc(bF(e.b));d.b.vd();){c=cI(Zhc(d),1);k=$D(i,c);fUb(j,0,a,c);wUb(b,0,a,'cw-DictionaryExample-header');fUb(j,1,a,k);wUb(b,1,a,'cw-DictionaryExample-data');++a}d5b(f,new NRb('<br><br>'));d5b(f,j);return f}
var gzc='userInfo';W1(345,1,{61:1},_D);_.fd=function bE(a){var b;b="Cannot find '"+a+"' in "+this;throw new Qlc(b)};_.tS=function dE(){return this.c};_.b=null;_.c=null;var WD;W1(643,1,foc);_.qc=function ykb(){z4(this.b,skb())};var vN=ucc(jvc,'Dictionary',345);Uoc(In)(32);