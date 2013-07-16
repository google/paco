function gG(){gG=m2c;fG=new R_c}
function hG(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.re(c)}}
function yzc(a,b,c,d){var e;a.b.ah(b,c);e=Azc(a.b.j,b,c);oj(e,d,true)}
function lG(){gG();var a;a=Dlb(fG.me(Red),61);if(!a){a=new kG;fG.oe(Red,a)}return a}
function iG(c,b){try{typeof $wnd[b]!='object'&&nG(b);c.b=$wnd[b]}catch(a){nG(b)}}
function nG(a){throw new S0c(Y6c+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function jG(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Vd(a);return String(c)}
function kG(){this.c='Dictionary userInfo';iG(this,Red);if(!this.b){throw new S0c("Cannot find JavaScript object with the name 'userInfo'")}}
function t_b(){var a,b,c,d,e,f,g,i,j,k,n;f=new iMc;g=new Pwc('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=r6c;g.db.style['textAlign']=L6c;fMc(f,new Pwc('<b>This example interacts with the following JavaScript variable:<\/b>'));fMc(f,g);j=new qzc;b=j.k;i=lG();e=(n=new Z_c,hG(i,n),n);a=0;for(d=VYc(FN(e.b));d.b.xe();){c=Dlb(_Yc(d),1);k=jG(i,c);hzc(j,0,a,c);yzc(b,0,a,'cw-DictionaryExample-header');hzc(j,1,a,k);yzc(b,1,a,'cw-DictionaryExample-data');++a}fMc(f,new Pwc('<br><br>'));fMc(f,j);return f}
var Red='userInfo';XIb(365,1,{61:1},kG);_.Vd=function mG(a){var b;b="Cannot find '"+a+"' in "+this;throw new S0c(b)};_.tS=function oG(){return this.c};_.b=null;_.c=null;var fG;XIb(726,1,h3c);_.qc=function z_b(){ALb(this.b,t_b())};var nrb=wTc(Mad,e8c,365);W3c(In)(32);