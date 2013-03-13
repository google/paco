function AE(){AE=kzc;zE=new Pwc}
function BE(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function H4b(a,b,c,d){var e;a.b.Yg(b,c);e=J4b(a.b.j,b,c);dj(e,d,true)}
function FE(){AE();var a;a=$T(zE.ie(oLc),61);if(!a){a=new EE;zE.ke(oLc,a)}return a}
function CE(c,b){try{typeof $wnd[b]!='object'&&HE(b);c.b=$wnd[b]}catch(a){HE(b)}}
function HE(a){throw new Qxc(WDc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function DE(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function EE(){this.c='Dictionary userInfo';CE(this,oLc);if(!this.b){throw new Qxc("Cannot find JavaScript object with the name 'userInfo'")}}
function Fwb(){var a,b,c,d,e,f,g,i,j,k,n;f=new rhc;g=new U1b('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=pDc;g.db.style['textAlign']=JDc;ohc(f,new U1b('<b>Cet exemple interagit avec le JavaScript variable suivant:<\/b>'));ohc(f,g);j=new z4b;b=j.k;i=FE();e=(n=new Xwc,BE(i,n),n);a=0;for(d=Ttc(CH(e.b));d.b.te();){c=$T(Ztc(d),1);k=DE(i,c);q4b(j,0,a,c);H4b(b,0,a,'cw-DictionaryExample-header');q4b(j,1,a,k);H4b(b,1,a,'cw-DictionaryExample-data');++a}ohc(f,new U1b('<br><br>'));ohc(f,j);return f}
var oLc='userInfo';heb(349,1,{61:1},EE);_.Rd=function GE(a){var b;b="Cannot find '"+a+"' in "+this;throw new Qxc(b)};_.tS=function IE(){return this.c};_.b=null;_.c=null;var zE;heb(663,1,eAc);_.mc=function Lwb(){Mgb(this.b,Fwb())};var tZ=uoc(hHc,'Dictionary',349);TAc(wn)(32);