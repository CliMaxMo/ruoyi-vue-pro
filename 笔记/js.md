在JavaScript中，使用import语句导入模块时，加上花括号{}与不加的区别在于：

不加花括号：导入整个模块对象。例如，import axios from 'axios'会导入整个axios模块，可以通过axios.get()等方法来使用它。

加上花括号：只导入模块中的指定变量或函数。例如，import { get, post } from 'axios'只导入了axios模块中的get和post方法，可以直接使用get()和post()调用它们。

需要注意的是，如果导入的模块没有默认导出（即没有export default语句），则必须使用花括号来指定导入的变量或函数。如果导入的模块有默认导出，则可以使用不加花括号的语法来导入整个模块对象。
