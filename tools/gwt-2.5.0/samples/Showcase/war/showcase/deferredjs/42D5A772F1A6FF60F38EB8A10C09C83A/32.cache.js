function RE(){RE=fAc;QE=new Kxc}
function SE(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function p5b(a,b,c,d){var e;a.a.Yg(b,c);e=r5b(a.a.i,b,c);cj(e,d,true)}
function WE(){RE();var a;a=pU(QE.ie(gMc),61);if(!a){a=new VE;QE.ke(gMc,a)}return a}
function TE(c,b){try{typeof $wnd[b]!='object'&&YE(b);c.a=$wnd[b]}catch(a){YE(b)}}
function YE(a){throw new Lyc(QEc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function UE(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function VE(){this.b='Dictionary userInfo';TE(this,gMc);if(!this.a){throw new Lyc("Cannot find JavaScript object with the name 'userInfo'")}}
function axb(){var a,b,c,d,e,f,g,i,j,k,n;f=new dic;g=new G2b('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=kEc;g.cb.style['textAlign']=EEc;aic(f,new G2b('<b>Cet exemple interagit avec le JavaScript variable suivant:<\/b>'));aic(f,g);j=new h5b;b=j.j;i=WE();e=(n=new Sxc,SE(i,n),n);a=0;for(d=Ouc(TH(e.a));d.a.te();){c=pU(Uuc(d),1);k=UE(i,c);$4b(j,0,a,c);p5b(b,0,a,'cw-DictionaryExample-header');$4b(j,1,a,k);p5b(b,1,a,'cw-DictionaryExample-data');++a}aic(f,new G2b('<br><br>'));aic(f,j);return f}
var gMc='userInfo';zeb(350,1,{61:1},VE);_.Rd=function XE(a){var b;b="Cannot find '"+a+"' in "+this;throw new Lyc(b)};_.tS=function ZE(){return this.b};_.a=null;_.b=null;var QE;zeb(665,1,_Ac);_.lc=function gxb(){hhb(this.a,axb())};var IZ=ppc(_Hc,'Dictionary',350);OBc(vn)(32);