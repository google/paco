function rF(){rF=XQc;qF=new AOc}
function fmc(a,b,c,d){var e;a.a.Yg(b,c);e=hmc(a.a.i,b,c);cj(e,d,true)}
function sF(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function uF(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function wF(){rF();var a;a=o8(qF.ie(g1c),61);if(!a){a=new vF;qF.ke(g1c,a)}return a}
function tF(c,b){try{typeof $wnd[b]!='object'&&yF(b);c.a=$wnd[b]}catch(a){yF(b)}}
function yF(a){throw new BPc(GVc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function vF(){this.b='Dictionary userInfo';tF(this,g1c);if(!this.a){throw new BPc("Cannot find JavaScript object with the name 'userInfo'")}}
function SNb(){var a,b,c,d,e,f,g,i,j,k,n;f=new Vyc;g=new wjc('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=aVc;g.cb.style['textAlign']=uVc;Syc(f,new wjc('<b>\u0647\u0630\u0627 \u0627\u0644\u0645\u062B\u0627\u0644 \u064A\u062A\u0641\u0627\u0639\u0644 \u0645\u0639 \u0645\u062A\u063A\u064A\u0631\u0627\u062A \u062C\u0627\u0641\u0627\u0633\u0643\u0631\u064A\u0628\u062A \u0627\u0644\u062A\u0627\u0644\u064A\u0629 :<\/b>'));Syc(f,g);j=new Zlc;b=j.j;i=wF();e=(n=new IOc,sF(i,n),n);a=0;for(d=ELc(BL(e.a));d.a.te();){c=o8(KLc(d),1);k=uF(i,c);Qlc(j,0,a,c);fmc(b,0,a,'cw-DictionaryExample-header');Qlc(j,1,a,k);fmc(b,1,a,'cw-DictionaryExample-data');++a}Syc(f,new wjc('<br><br>'));Syc(f,j);return f}
var g1c='userInfo';pvb(360,1,{61:1},vF);_.Rd=function xF(a){var b;b="Cannot find '"+a+"' in "+this;throw new BPc(b)};_.tS=function zF(){return this.b};_.a=null;_.b=null;var qF;pvb(710,1,RRc);_.lc=function YNb(){Zxb(this.a,SNb())};var Rdb=fGc(_Yc,'Dictionary',360);ESc(vn)(32);