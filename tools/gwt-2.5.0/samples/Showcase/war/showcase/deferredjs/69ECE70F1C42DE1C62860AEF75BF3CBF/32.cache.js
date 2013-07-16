function _E(){_E=aAc;$E=new Fxc}
function m5b(a,b,c,d){var e;a.b.ah(b,c);e=o5b(a.b.j,b,c);oj(e,d,true)}
function aF(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.re(c)}}
function cF(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Vd(a);return String(c)}
function eF(){_E();var a;a=zU($E.me(lMc),61);if(!a){a=new dF;$E.oe(lMc,a)}return a}
function bF(c,b){try{typeof $wnd[b]!='object'&&gF(b);c.b=$wnd[b]}catch(a){gF(b)}}
function gF(a){throw new Gyc(MEc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function dF(){this.c='Dictionary userInfo';bF(this,lMc);if(!this.b){throw new Gyc("Cannot find JavaScript object with the name 'userInfo'")}}
function hxb(){var a,b,c,d,e,f,g,i,j,k,n;f=new Yhc;g=new D2b('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=fEc;g.db.style['textAlign']=zEc;Vhc(f,new D2b('<b>Cet exemple interagit avec le JavaScript variable suivant:<\/b>'));Vhc(f,g);j=new e5b;b=j.k;i=eF();e=(n=new Nxc,aF(i,n),n);a=0;for(d=Juc(bI(e.b));d.b.xe();){c=zU(Puc(d),1);k=cF(i,c);X4b(j,0,a,c);m5b(b,0,a,'cw-DictionaryExample-header');X4b(j,1,a,k);m5b(b,1,a,'cw-DictionaryExample-data');++a}Vhc(f,new D2b('<br><br>'));Vhc(f,j);return f}
var lMc='userInfo';Leb(350,1,{61:1},dF);_.Vd=function fF(a){var b;b="Cannot find '"+a+"' in "+this;throw new Gyc(b)};_.tS=function hF(){return this.c};_.b=null;_.c=null;var $E;Leb(664,1,XAc);_.qc=function nxb(){ohb(this.b,hxb())};var WZ=kpc(eIc,'Dictionary',350);KBc(In)(32);