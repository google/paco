function GE(){GE=Nzc;FE=new qxc}
function Z4b(a,b,c,d){var e;a.a.Yg(b,c);e=_4b(a.a.i,b,c);dj(e,d,true)}
function HE(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function JE(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function LE(){GE();var a;a=eU(FE.ie(MLc),61);if(!a){a=new KE;FE.ke(MLc,a)}return a}
function IE(c,b){try{typeof $wnd[b]!='object'&&NE(b);c.a=$wnd[b]}catch(a){NE(b)}}
function NE(a){throw new ryc(uEc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function KE(){this.b='Dictionary userInfo';IE(this,MLc);if(!this.a){throw new ryc("Cannot find JavaScript object with the name 'userInfo'")}}
function Rwb(){var a,b,c,d,e,f,g,i,j,k,n;f=new Nhc;g=new o2b('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=PDc;g.cb.style['textAlign']=hEc;Khc(f,new o2b('<b>Cet exemple interagit avec le JavaScript variable suivant:<\/b>'));Khc(f,g);j=new R4b;b=j.j;i=LE();e=(n=new yxc,HE(i,n),n);a=0;for(d=uuc(IH(e.a));d.a.te();){c=eU(Auc(d),1);k=JE(i,c);I4b(j,0,a,c);Z4b(b,0,a,'cw-DictionaryExample-header');I4b(j,1,a,k);Z4b(b,1,a,'cw-DictionaryExample-data');++a}Khc(f,new o2b('<br><br>'));Khc(f,j);return f}
var MLc='userInfo';oeb(351,1,{61:1},KE);_.Rd=function ME(a){var b;b="Cannot find '"+a+"' in "+this;throw new ryc(b)};_.tS=function OE(){return this.b};_.a=null;_.b=null;var FE;oeb(666,1,HAc);_.lc=function Xwb(){Ygb(this.a,Rwb())};var xZ=Xoc(GHc,'Dictionary',351);uBc(wn)(32);