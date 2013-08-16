function BF(){BF=SQc;AF=new vOc}
function cmc(a,b,c,d){var e;a.b.ah(b,c);e=emc(a.b.j,b,c);oj(e,d,true)}
function CF(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.re(c)}}
function EF(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Vd(a);return String(c)}
function GF(){BF();var a;a=y8(AF.me(l1c),61);if(!a){a=new FF;AF.oe(l1c,a)}return a}
function DF(c,b){try{typeof $wnd[b]!='object'&&IF(b);c.b=$wnd[b]}catch(a){IF(b)}}
function IF(a){throw new wPc(CVc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function FF(){this.c='Dictionary userInfo';DF(this,l1c);if(!this.b){throw new wPc("Cannot find JavaScript object with the name 'userInfo'")}}
function ZNb(){var a,b,c,d,e,f,g,i,j,k,n;f=new Oyc;g=new tjc('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=XUc;g.db.style['textAlign']=pVc;Lyc(f,new tjc('<b>\u0647\u0630\u0627 \u0627\u0644\u0645\u062B\u0627\u0644 \u064A\u062A\u0641\u0627\u0639\u0644 \u0645\u0639 \u0645\u062A\u063A\u064A\u0631\u0627\u062A \u062C\u0627\u0641\u0627\u0633\u0643\u0631\u064A\u0628\u062A \u0627\u0644\u062A\u0627\u0644\u064A\u0629 :<\/b>'));Lyc(f,g);j=new Wlc;b=j.k;i=GF();e=(n=new DOc,CF(i,n),n);a=0;for(d=zLc(LL(e.b));d.b.xe();){c=y8(FLc(d),1);k=EF(i,c);Nlc(j,0,a,c);cmc(b,0,a,'cw-DictionaryExample-header');Nlc(j,1,a,k);cmc(b,1,a,'cw-DictionaryExample-data');++a}Lyc(f,new tjc('<br><br>'));Lyc(f,j);return f}
var l1c='userInfo';Bvb(360,1,{61:1},FF);_.Vd=function HF(a){var b;b="Cannot find '"+a+"' in "+this;throw new wPc(b)};_.tS=function JF(){return this.c};_.b=null;_.c=null;var AF;Bvb(709,1,NRc);_.qc=function dOb(){eyb(this.b,ZNb())};var deb=aGc(eZc,'Dictionary',360);ASc(In)(32);