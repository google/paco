function aF(){aF=aQc;_E=new FNc}
function xlc(a,b,c,d){var e;a.b.Yg(b,c);e=zlc(a.b.j,b,c);dj(e,d,true)}
function bF(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function dF(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function fF(){aF();var a;a=Z7(_E.ie(o0c),61);if(!a){a=new eF;_E.ke(o0c,a)}return a}
function cF(c,b){try{typeof $wnd[b]!='object'&&hF(b);c.b=$wnd[b]}catch(a){hF(b)}}
function hF(a){throw new GOc(MUc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function eF(){this.c='Dictionary userInfo';cF(this,o0c);if(!this.b){throw new GOc("Cannot find JavaScript object with the name 'userInfo'")}}
function vNb(){var a,b,c,d,e,f,g,i,j,k,n;f=new hyc;g=new Kic('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=fUc;g.db.style['textAlign']=zUc;eyc(f,new Kic('<b>\u0647\u0630\u0627 \u0627\u0644\u0645\u062B\u0627\u0644 \u064A\u062A\u0641\u0627\u0639\u0644 \u0645\u0639 \u0645\u062A\u063A\u064A\u0631\u0627\u062A \u062C\u0627\u0641\u0627\u0633\u0643\u0631\u064A\u0628\u062A \u0627\u0644\u062A\u0627\u0644\u064A\u0629 :<\/b>'));eyc(f,g);j=new plc;b=j.k;i=fF();e=(n=new NNc,bF(i,n),n);a=0;for(d=JKc(kL(e.b));d.b.te();){c=Z7(PKc(d),1);k=dF(i,c);glc(j,0,a,c);xlc(b,0,a,'cw-DictionaryExample-header');glc(j,1,a,k);xlc(b,1,a,'cw-DictionaryExample-data');++a}eyc(f,new Kic('<br><br>'));eyc(f,j);return f}
var o0c='userInfo';Zub(359,1,{61:1},eF);_.Rd=function gF(a){var b;b="Cannot find '"+a+"' in "+this;throw new GOc(b)};_.tS=function iF(){return this.c};_.b=null;_.c=null;var _E;Zub(708,1,WQc);_.mc=function BNb(){Cxb(this.b,vNb())};var Cdb=kFc(hYc,'Dictionary',359);JRc(wn)(32);