function gF(){gF=DQc;fF=new gOc}
function Plc(a,b,c,d){var e;a.a.Yg(b,c);e=Rlc(a.a.i,b,c);dj(e,d,true)}
function hF(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function jF(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function lF(){gF();var a;a=d8(fF.ie(M0c),61);if(!a){a=new kF;fF.ke(M0c,a)}return a}
function iF(c,b){try{typeof $wnd[b]!='object'&&nF(b);c.a=$wnd[b]}catch(a){nF(b)}}
function nF(a){throw new hPc(kVc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function kF(){this.b='Dictionary userInfo';iF(this,M0c);if(!this.a){throw new hPc("Cannot find JavaScript object with the name 'userInfo'")}}
function HNb(){var a,b,c,d,e,f,g,i,j,k,n;f=new Dyc;g=new ejc('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=FUc;g.cb.style['textAlign']=ZUc;Ayc(f,new ejc('<b>\u0647\u0630\u0627 \u0627\u0644\u0645\u062B\u0627\u0644 \u064A\u062A\u0641\u0627\u0639\u0644 \u0645\u0639 \u0645\u062A\u063A\u064A\u0631\u0627\u062A \u062C\u0627\u0641\u0627\u0633\u0643\u0631\u064A\u0628\u062A \u0627\u0644\u062A\u0627\u0644\u064A\u0629 :<\/b>'));Ayc(f,g);j=new Hlc;b=j.j;i=lF();e=(n=new oOc,hF(i,n),n);a=0;for(d=kLc(qL(e.a));d.a.te();){c=d8(qLc(d),1);k=jF(i,c);ylc(j,0,a,c);Plc(b,0,a,'cw-DictionaryExample-header');ylc(j,1,a,k);Plc(b,1,a,'cw-DictionaryExample-data');++a}Ayc(f,new ejc('<br><br>'));Ayc(f,j);return f}
var M0c='userInfo';evb(361,1,{61:1},kF);_.Rd=function mF(a){var b;b="Cannot find '"+a+"' in "+this;throw new hPc(b)};_.tS=function oF(){return this.b};_.a=null;_.b=null;var fF;evb(711,1,xRc);_.lc=function NNb(){Oxb(this.a,HNb())};var Gdb=NFc(GYc,'Dictionary',361);kSc(wn)(32);