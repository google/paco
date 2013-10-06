function mF(){mF=yAc;lF=new byc}
function C5b(a,b,c,d){var e;a.b.eh(b,c);e=E5b(a.b.j,b,c);pj(e,d,true)}
function nF(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.ve(c)}}
function pF(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Zd(a);return String(c)}
function rF(){mF();var a;a=MU(lF.qe(JMc),61);if(!a){a=new qF;lF.se(JMc,a)}return a}
function oF(c,b){try{typeof $wnd[b]!='object'&&tF(b);c.b=$wnd[b]}catch(a){tF(b)}}
function tF(a){throw new czc(pFc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function qF(){this.c='Dictionary userInfo';oF(this,JMc);if(!this.b){throw new czc("Cannot find JavaScript object with the name 'userInfo'")}}
function Axb(){var a,b,c,d,e,f,g,i,j,k,n;f=new pic;g=new T2b('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=OEc;g.db.style['textAlign']=cFc;mic(f,new T2b('<b>Cet exemple interagit avec le JavaScript variable suivant:<\/b>'));mic(f,g);j=new u5b;b=j.k;i=rF();e=(n=new jyc,nF(i,n),n);a=0;for(d=fvc(oI(e.b));d.b.Be();){c=MU(lvc(d),1);k=pF(i,c);l5b(j,0,a,c);C5b(b,0,a,'cw-DictionaryExample-header');l5b(j,1,a,k);C5b(b,1,a,'cw-DictionaryExample-data');++a}mic(f,new T2b('<br><br>'));mic(f,j);return f}
var JMc='userInfo';cfb(354,1,{61:1},qF);_.Zd=function sF(a){var b;b="Cannot find '"+a+"' in "+this;throw new czc(b)};_.tS=function uF(){return this.c};_.b=null;_.c=null;var lF;cfb(668,1,tBc);_.qc=function Gxb(){Hhb(this.b,Axb())};var l$=Hpc(CIc,'Dictionary',354);gCc(Jn)(32);