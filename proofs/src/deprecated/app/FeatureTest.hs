﻿-- featureTest :: FeatureAnnotation f => String -> String -> f -> f -> IO()
-- featureTest nameOfA nameOfB a b = do
--     putStrLn ("  " ++ nameOfA ++ " == " ++ nameOfB)
--     putStrLn ("= " ++ show a ++ " == " ++ show b)
--     putStrLn ("= " ++ show (equivalent a b))

-- featureTestCase1 :: IO()
-- featureTestCase1 = do
--     featureTest "A" "A" featureA featureA
--     featureTest "A" "B" featureA featureB
--     featureTest "(A<>B)" "(A<>B)" (featureA <> featureB) (featureA <> featureB)
--     featureTest "(A<>B)" "(B<>A)" (featureA <> featureB) (featureB <> featureA)
--     featureTest "(A <> C)" "B" (featureA <> featureC) featureB