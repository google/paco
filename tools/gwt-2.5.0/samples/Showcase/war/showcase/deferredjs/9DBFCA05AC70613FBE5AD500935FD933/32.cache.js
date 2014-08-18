function CD(){CD=Xmc;BD=new Akc}
function hUb(a,b,c,d){var e;a.a.Wf(b,c);e=jUb(a.a.i,b,c);dj(e,d,true)}
function DD(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ld(c)}}
function FD(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.bd(a);return String(c)}
function HD(){CD();var a;a=JH(BD.fd(Hyc),61);if(!a){a=new GD;BD.hd(Hyc,a)}return a}
function ED(c,b){try{typeof $wnd[b]!='object'&&JD(b);c.a=$wnd[b]}catch(a){JD(b)}}
function JD(a){throw new Blc(Erc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function GD(){this.b='Dictionary userInfo';ED(this,Hyc);if(!this.a){throw new Blc("Cannot find JavaScript object with the name 'userInfo'")}}
function akb(){var a,b,c,d,e,f,g,i,j,k,n;f=new X4b;g=new yRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=Zqc;g.cb.style['textAlign']=rrc;U4b(f,new yRb('<b>\u8FD9\u4E2A\u4F8B\u5B50\u4F7F\u7528\u4E0B\u5217Javascript\u7684\u53D8\u91CF\uFF1A <\/b>'));U4b(f,g);j=new _Tb;b=j.j;i=HD();e=(n=new Ikc,DD(i,n),n);a=0;for(d=Ehc(IE(e.a));d.a.rd();){c=JH(Khc(d),1);k=FD(i,c);STb(j,0,a,c);hUb(b,0,a,'cw-DictionaryExample-header');STb(j,1,a,k);hUb(b,1,a,'cw-DictionaryExample-data');++a}U4b(f,new yRb('<br><br>'));U4b(f,j);return f}
var Hyc='userInfo';z1(346,1,{61:1},GD);_.bd=function ID(a){var b;b="Cannot find '"+a+"' in "+this;throw new Blc(b)};_.tS=function KD(){return this.b};_.a=null;_.b=null;var BD;z1(645,1,Rnc);_.lc=function gkb(){h4(this.a,akb())};var YM=fcc(Luc,'Dictionary',346);Eoc(wn)(32);