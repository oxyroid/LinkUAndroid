package com.wzk.domain.repository.food

import com.wzk.domain.entity.Food
import com.wzk.wrapper.Result

class FoodRepositoryMock : FoodRepository {
    private val foods = mutableListOf(
        Food(
            0,
            "Designart 马和彩虹动物抱枕客厅、沙发、枕芯 + 双面印花 40.64 厘米 x 40.64 厘米",
            "黄色的水果",
            98f,
            "https://cbu01.alicdn.com/img/ibank/2018/617/111/8854111716_1435007588.jpg"
        ),
        Food(
            1,
            "Snow Peak 雪峰 钛金属单层杯",
            "shuibei",
            161f,
            "https://images-cn.ssl-images-amazon.cn/images/I/51D7ApjpprL._AC_SX679_.jpg"
        ),
        Food(
            2,
            "肌肉博士潮流健身时尚训练舒适透气夏季运动休闲短裤男士厂家直销",
            "好吃的",
            16f,
            "https://cbu01.alicdn.com/img/ibank/2018/256/358/9285853652_943463140.jpg"
        ),
        Food(
            3,
            "《深入理解Kotlin协程》",
            "好吃的",
            79f,
            "https://img.alicdn.com/imgextra/i1/1049653664/O1CN01DvXdsF1cw9nAYPKHQ_!!0-item_pic.jpg_430x430q90.jpg"
        ),
        Food(
            4,
            "耐克乔丹新款夏季男士运动速干短裤梭织轻薄透气休闲跑步健身篮球",
            """
                全新新款男士运动速干短裤夏季梭织轻薄透气休闲篮球健身跑步五分裤
                两个款式，三个颜色，尺码全
                尺码  s~3x
                五分裤 
                S         建议80-100斤
                M        建议100-115斤
                L         建议115-130斤
                XL       建议130-145斤
                XXL     建议145-160斤
                XXXL   建议160-175斤
                图片三个色都有！
                除偏远地区都包邮哦！宽松版型 运动休闲很舒适
                具体可咨询我吧！！

            """.trimIndent(),
            39f,
            "https://gsnapshot.alicdn.com/imgextra/i3/O1CN01wTzI2Y1OW8ovZL2ww_!!0-fleamarket.jpg_430x430.jpg"
        ),
        Food(
            5,
            "RK61无线小机械键盘蓝牙61键便携式办公笔记本家用MAC电竞68有线PBT键帽电脑青轴红轴茶轴客制化三模迷你",
            "好吃的",
            109f,
            "https://img.alicdn.com/imgextra/i2/3029409806/O1CN018xaf7N2MJCVJrTyEj_!!3029409806.jpg_430x430q90.jpg"
        ),
    )

    override suspend fun getById(id: Int): Result<Food> {
        return foods.find { it.id == id }.let { food ->
            Result(
                data = food,
                code = food?.let { 200 } ?: 400
            )
        }
    }

    override suspend fun getAll(): Result<List<Food>> {
        return Result(foods)
    }

    override suspend fun put(food: Food): Result<Food> {
        return if (foods.find { it.id == food.id } != null) Result(code = 400)
        else {
            foods.add(food)
            Result(data = food)
        }
    }
}